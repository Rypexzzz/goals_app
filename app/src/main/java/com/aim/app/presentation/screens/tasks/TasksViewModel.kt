package com.aim.app.presentation.screens.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aim.app.domain.usecase.task.CompleteTaskUseCase
import com.aim.app.domain.usecase.task.GetDeadlineTasksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TasksViewModel @Inject constructor(
    getDeadlineTasks: GetDeadlineTasksUseCase,
    private val completeTask: CompleteTaskUseCase,
) : ViewModel() {

    val uiState: StateFlow<TasksUiState> = getDeadlineTasks()
        .map { TasksUiState(tasks = it, isLoading = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TasksUiState(),
        )

    fun onComplete(taskId: Long) = viewModelScope.launch { completeTask(taskId) }
}
