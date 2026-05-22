package com.aim.app.presentation.screens.taskdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.aim.app.domain.model.Task
import com.aim.app.domain.model.TaskNode
import com.aim.app.domain.model.TaskStatus
import com.aim.app.domain.repository.TaskRepository
import com.aim.app.domain.usecase.goal.ObserveGoalUseCase
import com.aim.app.domain.usecase.task.CompleteTaskUseCase
import com.aim.app.domain.usecase.task.ObserveTaskUseCase
import com.aim.app.domain.usecase.task.ObserveTasksForGoalUseCase
import com.aim.app.domain.usecase.task.ReorderTasksUseCase
import com.aim.app.domain.usecase.task.SoftDeleteTaskUseCase
import com.aim.app.domain.usecase.task.UncompleteTaskUseCase
import com.aim.app.presentation.navigation.AimRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val observeTask: ObserveTaskUseCase,
    private val observeGoal: ObserveGoalUseCase,
    private val observeTasksForGoal: ObserveTasksForGoalUseCase,
    private val taskRepository: TaskRepository,
    private val completeTask: CompleteTaskUseCase,
    private val uncompleteTask: UncompleteTaskUseCase,
    private val softDeleteTask: SoftDeleteTaskUseCase,
    private val reorderTasks: ReorderTasksUseCase,
) : ViewModel() {

    private val route: AimRoute.TaskDetail = savedStateHandle.toRoute()
    val taskId: Long = route.taskId

    private val expandedIds = MutableStateFlow<Set<Long>>(emptySet())
    private val confirmDeleteFlow = MutableStateFlow(false)

    private val taskFlow = observeTask(taskId)

    val uiState: StateFlow<TaskDetailUiState> = taskFlow
        .flatMapLatest { task ->
            if (task == null) {
                flowOf(TaskDetailUiState(isLoading = false, finished = true))
            } else {
                combine(
                    observeGoal(task.goalId),
                    observeTasksForGoal(task.goalId),
                    expandedIds,
                    confirmDeleteFlow,
                ) { goal, allRoots, expanded, confirmDelete ->
                    val subtaskRoots = findSubtree(allRoots, task.id)?.children.orEmpty()
                    TaskDetailUiState(
                        isLoading = false,
                        task = task,
                        goal = goal,
                        breadcrumbs = buildBreadcrumbs(allRoots, task, goal?.title.orEmpty(), goal?.emoji),
                        subtaskRoots = subtaskRoots,
                        expandedTaskIds = expanded,
                        confirmDelete = confirmDelete,
                        finished = task.deletedAt != null,
                    )
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TaskDetailUiState(),
        )

    fun onToggleExpand(taskId: Long) = expandedIds.update { ids ->
        if (taskId in ids) ids - taskId else ids + taskId
    }

    fun onToggleCompletion() = viewModelScope.launch {
        val task = uiState.value.task ?: return@launch
        if (task.status == TaskStatus.COMPLETED) uncompleteTask(task.id) else completeTask(task.id)
    }

    fun onToggleChildCompletion(task: Task) = viewModelScope.launch {
        if (task.status == TaskStatus.COMPLETED) uncompleteTask(task.id) else completeTask(task.id)
    }

    fun onDeleteChild(taskId: Long) = viewModelScope.launch { softDeleteTask(taskId) }

    fun onReorderSiblings(parentTaskId: Long?, orderedIds: List<Long>) = viewModelScope.launch {
        val task = uiState.value.task ?: return@launch
        reorderTasks(parentTaskId = parentTaskId, goalId = task.goalId, orderedIds = orderedIds)
    }

    fun requestDelete() = confirmDeleteFlow.update { true }
    fun dismissDelete() = confirmDeleteFlow.update { false }
    fun confirmDelete() = viewModelScope.launch {
        confirmDeleteFlow.update { false }
        softDeleteTask(taskId)
    }

    private fun findSubtree(roots: List<TaskNode>, taskId: Long): TaskNode? {
        roots.forEach { node ->
            if (node.task.id == taskId) return node
            findSubtree(node.children, taskId)?.let { return it }
        }
        return null
    }

    /**
     * Хлебные крошки: Цель → … родители … → текущая задача.
     */
    private fun buildBreadcrumbs(
        roots: List<TaskNode>,
        task: Task,
        goalTitle: String,
        goalEmoji: String?,
    ): List<Breadcrumb> {
        val parents = mutableListOf<Task>()
        var current: Long? = task.parentTaskId
        while (current != null) {
            val node = findSubtree(roots, current) ?: break
            parents.add(0, node.task)
            current = node.task.parentTaskId
        }
        return buildList {
            add(Breadcrumb(taskId = null, title = goalTitle, emoji = goalEmoji))
            parents.forEach { add(Breadcrumb(taskId = it.id, title = it.title, emoji = it.emoji)) }
            add(Breadcrumb(taskId = task.id, title = task.title, emoji = task.emoji))
        }
    }
}
