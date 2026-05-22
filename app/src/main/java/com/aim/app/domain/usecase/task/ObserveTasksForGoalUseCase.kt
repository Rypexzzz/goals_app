package com.aim.app.domain.usecase.task

import com.aim.app.domain.model.Task
import com.aim.app.domain.model.TaskNode
import com.aim.app.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObserveTasksForGoalUseCase @Inject constructor(
    private val repository: TaskRepository,
) {
    /**
     * Возвращает дерево задач (списком корней) для цели. Корни — задачи с `parentTaskId == null`,
     * упорядочены по `orderIndex`. Каждое поддерево также упорядочено внутри.
     */
    operator fun invoke(goalId: Long): Flow<List<TaskNode>> =
        repository.observeActiveTasksForGoal(goalId).map { it.buildForest() }

    private fun List<Task>.buildForest(): List<TaskNode> {
        val byParent = groupBy { it.parentTaskId }
        fun nodesFor(parentId: Long?): List<TaskNode> =
            byParent[parentId]
                ?.sortedBy { it.orderIndex }
                ?.map { task -> TaskNode(task, nodesFor(task.id)) }
                .orEmpty()
        return nodesFor(null)
    }
}
