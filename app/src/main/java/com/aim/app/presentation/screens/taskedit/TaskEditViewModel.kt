package com.aim.app.presentation.screens.taskedit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aim.app.domain.model.Recurrence
import com.aim.app.domain.model.Task
import com.aim.app.domain.usecase.task.CreateTaskUseCase
import com.aim.app.domain.usecase.task.ObserveTaskUseCase
import com.aim.app.domain.usecase.task.UpdateTaskUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

@HiltViewModel(assistedFactory = TaskEditViewModel.Factory::class)
class TaskEditViewModel @AssistedInject constructor(
    @Assisted private val mode: TaskEditMode,
    private val createTask: CreateTaskUseCase,
    private val updateTask: UpdateTaskUseCase,
    private val observeTask: ObserveTaskUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        TaskEditUiState(isLoading = mode is TaskEditMode.Edit, isExisting = mode is TaskEditMode.Edit),
    )
    val uiState: StateFlow<TaskEditUiState> = _uiState.asStateFlow()

    private var editingTask: Task? = null

    init {
        if (mode is TaskEditMode.Edit) {
            viewModelScope.launch {
                val task = observeTask(mode.taskId).firstOrNull()
                if (task != null) {
                    editingTask = task
                    _uiState.value = TaskEditUiState(
                        isLoading = false,
                        isExisting = true,
                        title = task.title,
                        description = task.description.orEmpty(),
                        emoji = task.emoji,
                        scheduledFor = task.scheduledFor,
                        scheduledTime = task.scheduledTime,
                        deadline = task.deadline,
                        recurrence = task.recurrence,
                        canSave = task.title.isNotBlank(),
                    )
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    fun onTitleChange(value: String) = _uiState.update {
        it.copy(title = value, canSave = value.trim().isNotEmpty())
    }
    fun onDescriptionChange(value: String) = _uiState.update { it.copy(description = value) }
    fun onEmojiChange(value: String?) = _uiState.update { it.copy(emoji = value) }
    fun onScheduledForChange(value: LocalDate?) = _uiState.update { it.copy(scheduledFor = value) }
    fun onScheduledTimeChange(value: LocalTime?) = _uiState.update { it.copy(scheduledTime = value) }
    fun onDeadlineChange(value: LocalDate?) = _uiState.update { it.copy(deadline = value) }
    fun onRecurrenceChange(value: Recurrence?) = _uiState.update { it.copy(recurrence = value) }

    fun onSave() {
        val current = _uiState.value
        if (!current.canSave) return
        viewModelScope.launch {
            when (val ctx = mode) {
                is TaskEditMode.Create -> {
                    createTask(
                        goalId = ctx.goalId,
                        parentTaskId = ctx.parentTaskId,
                        title = current.title,
                        description = current.description.takeIf { it.isNotBlank() },
                        emoji = current.emoji,
                        deadline = current.deadline,
                        scheduledFor = current.scheduledFor,
                        scheduledTime = current.scheduledTime,
                        recurrence = current.recurrence,
                    )
                }
                is TaskEditMode.Edit -> {
                    val original = editingTask ?: return@launch
                    updateTask(
                        original.copy(
                            title = current.title,
                            description = current.description.takeIf { it.isNotBlank() },
                            emoji = current.emoji,
                            scheduledFor = current.scheduledFor,
                            scheduledTime = current.scheduledTime,
                            deadline = current.deadline,
                            recurrence = current.recurrence,
                        ),
                    )
                }
            }
            _uiState.update { it.copy(saved = true) }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(mode: TaskEditMode): TaskEditViewModel
    }
}
