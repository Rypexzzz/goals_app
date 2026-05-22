package com.aim.app.presentation.screens.goaldetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.aim.app.domain.model.Task
import com.aim.app.domain.model.TaskNode
import com.aim.app.domain.model.TaskStatus
import com.aim.app.domain.usecase.goal.ArchiveGoalUseCase
import com.aim.app.domain.usecase.goal.CompleteGoalUseCase
import com.aim.app.domain.usecase.goal.ObserveGoalUseCase
import com.aim.app.domain.usecase.goal.SoftDeleteGoalUseCase
import com.aim.app.domain.usecase.goal.UncompleteGoalUseCase
import com.aim.app.domain.usecase.habit.ObserveHabitsForGoalUseCase
import com.aim.app.domain.usecase.task.CompleteTaskUseCase
import com.aim.app.domain.usecase.task.ObserveTasksForGoalUseCase
import com.aim.app.domain.usecase.task.ReorderTasksUseCase
import com.aim.app.domain.usecase.task.SoftDeleteTaskUseCase
import com.aim.app.domain.usecase.task.UncompleteTaskUseCase
import com.aim.app.presentation.navigation.AimRoute
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
class GoalDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeGoal: ObserveGoalUseCase,
    observeTasksForGoal: ObserveTasksForGoalUseCase,
    observeHabitsForGoal: ObserveHabitsForGoalUseCase,
    private val softDeleteGoal: SoftDeleteGoalUseCase,
    private val archiveGoal: ArchiveGoalUseCase,
    private val completeGoal: CompleteGoalUseCase,
    private val uncompleteGoal: UncompleteGoalUseCase,
    private val completeTask: CompleteTaskUseCase,
    private val uncompleteTask: UncompleteTaskUseCase,
    private val softDeleteTask: SoftDeleteTaskUseCase,
    private val reorderTasks: ReorderTasksUseCase,
) : ViewModel() {

    private val route: AimRoute.GoalDetail = savedStateHandle.toRoute()
    val goalId: Long = route.goalId

    private val expandedIds = MutableStateFlow<Set<Long>>(emptySet())
    private val dialogs = MutableStateFlow(DialogState())

    val uiState: StateFlow<GoalDetailUiState> = combine(
        observeGoal(goalId),
        observeTasksForGoal(goalId),
        observeHabitsForGoal(goalId),
        expandedIds,
        dialogs,
    ) { goal, roots, habits, expanded, dialog ->
        if (goal == null) {
            GoalDetailUiState(isLoading = false, finished = true)
        } else {
            GoalDetailUiState(
                isLoading = false,
                goal = goal,
                taskRoots = roots,
                expandedTaskIds = expanded,
                linkedHabits = habits,
                pendingTasks = roots.flatMap { it.allTasks() }
                    .filter { it.status == TaskStatus.IN_PROGRESS },
                confirmDelete = dialog.deleteOpen,
                confirmCompleteWithChildren = dialog.completeOpen,
                finished = goal.deletedAt != null,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = GoalDetailUiState(),
    )

    fun onToggleExpand(taskId: Long) = expandedIds.update { ids ->
        if (taskId in ids) ids - taskId else ids + taskId
    }

    fun onToggleTaskCompletion(task: Task) = viewModelScope.launch {
        if (task.status == TaskStatus.COMPLETED) uncompleteTask(task.id) else completeTask(task.id)
    }

    fun onDeleteTask(taskId: Long) = viewModelScope.launch { softDeleteTask(taskId) }

    fun onReorderSiblings(parentTaskId: Long?, orderedIds: List<Long>) = viewModelScope.launch {
        reorderTasks(parentTaskId = parentTaskId, goalId = goalId, orderedIds = orderedIds)
    }

    fun requestDeleteGoal() = dialogs.update { it.copy(deleteOpen = true) }
    fun dismissDeleteDialog() = dialogs.update { it.copy(deleteOpen = false) }
    fun confirmDeleteGoal() = viewModelScope.launch {
        dialogs.update { it.copy(deleteOpen = false) }
        softDeleteGoal(goalId)
    }

    fun onArchiveGoal() = viewModelScope.launch { archiveGoal(goalId) }
    fun onUncompleteGoal() = viewModelScope.launch { uncompleteGoal(goalId) }

    fun requestCompleteGoal() {
        val state = uiState.value
        if (state.pendingTasks.isNotEmpty()) {
            dialogs.update { it.copy(completeOpen = true) }
        } else {
            completeGoalOnly()
        }
    }

    fun dismissCompleteDialog() = dialogs.update { it.copy(completeOpen = false) }

    fun completeGoalOnly() = viewModelScope.launch {
        dialogs.update { it.copy(completeOpen = false) }
        completeGoal(goalId)
    }

    fun completeGoalAndPendingTasks() = viewModelScope.launch {
        dialogs.update { it.copy(completeOpen = false) }
        uiState.value.pendingTasks.forEach { completeTask(it.id) }
        completeGoal(goalId)
    }

    private data class DialogState(
        val deleteOpen: Boolean = false,
        val completeOpen: Boolean = false,
    )
}

private fun TaskNode.allTasks(): List<Task> = buildList {
    add(task)
    children.forEach { addAll(it.allTasks()) }
}
