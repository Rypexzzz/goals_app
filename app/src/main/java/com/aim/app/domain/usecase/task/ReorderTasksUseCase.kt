package com.aim.app.domain.usecase.task

import com.aim.app.domain.repository.TaskRepository
import javax.inject.Inject

class ReorderTasksUseCase @Inject constructor(
    private val repository: TaskRepository,
) {
    /**
     * Изменяет `orderIndex` задач в пределах одного `parentTaskId` под одной целью.
     * Все `orderedIds` должны принадлежать одному и тому же родителю и одной цели.
     */
    suspend operator fun invoke(parentTaskId: Long?, goalId: Long, orderedIds: List<Long>) {
        repository.reorder(parentTaskId = parentTaskId, goalId = goalId, orderedIds = orderedIds)
    }
}
