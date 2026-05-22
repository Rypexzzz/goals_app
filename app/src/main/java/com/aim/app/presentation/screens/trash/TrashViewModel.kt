package com.aim.app.presentation.screens.trash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aim.app.domain.model.TrashItem
import com.aim.app.domain.usecase.goal.PermanentlyDeleteGoalUseCase
import com.aim.app.domain.usecase.goal.RestoreGoalUseCase
import com.aim.app.domain.usecase.task.PermanentlyDeleteTaskUseCase
import com.aim.app.domain.usecase.task.RestoreTaskUseCase
import com.aim.app.domain.usecase.trash.EmptyTrashUseCase
import com.aim.app.domain.usecase.trash.ObserveTrashUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrashViewModel @Inject constructor(
    observeTrash: ObserveTrashUseCase,
    private val restoreGoal: RestoreGoalUseCase,
    private val restoreTask: RestoreTaskUseCase,
    private val permanentlyDeleteGoal: PermanentlyDeleteGoalUseCase,
    private val permanentlyDeleteTask: PermanentlyDeleteTaskUseCase,
    private val emptyTrashUseCase: EmptyTrashUseCase,
) : ViewModel() {

    private val confirmEmptyFlow = MutableStateFlow(false)
    private val pendingDeleteFlow = MutableStateFlow<TrashItem?>(null)

    val uiState: StateFlow<TrashUiState> = combine(
        observeTrash(),
        confirmEmptyFlow,
        pendingDeleteFlow,
    ) { items, empty, pending ->
        TrashUiState(
            items = items,
            isLoading = false,
            confirmEmpty = empty,
            pendingDelete = pending,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TrashUiState(),
    )

    fun onRestore(item: TrashItem) = viewModelScope.launch {
        when (item) {
            is TrashItem.GoalItem -> restoreGoal(item.goal.id)
            is TrashItem.TaskItem -> restoreTask(item.task.id)
        }
    }

    fun requestDelete(item: TrashItem) = pendingDeleteFlow.update { item }
    fun dismissDelete() = pendingDeleteFlow.update { null }
    fun confirmDelete() = viewModelScope.launch {
        val item = pendingDeleteFlow.value ?: return@launch
        pendingDeleteFlow.update { null }
        when (item) {
            is TrashItem.GoalItem -> permanentlyDeleteGoal(item.goal.id)
            is TrashItem.TaskItem -> permanentlyDeleteTask(item.task.id)
        }
    }

    fun requestEmpty() = confirmEmptyFlow.update { true }
    fun dismissEmpty() = confirmEmptyFlow.update { false }
    fun confirmEmpty() = viewModelScope.launch {
        confirmEmptyFlow.update { false }
        emptyTrashUseCase(uiState.value.items)
    }
}
