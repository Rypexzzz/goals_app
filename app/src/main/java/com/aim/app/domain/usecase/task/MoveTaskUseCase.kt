package com.aim.app.domain.usecase.task

import com.aim.app.domain.repository.TaskRepository
import javax.inject.Inject

class MoveTaskUseCase @Inject constructor(
    private val repository: TaskRepository,
) {
    /**
     * Сменить родителя задачи. Циклы и превышение [com.aim.app.domain.model.Task.MAX_DEPTH]
     * детектирует репозиторий и кидает [IllegalStateException].
     */
    suspend operator fun invoke(taskId: Long, newParentId: Long?, newGoalId: Long) {
        repository.moveTask(taskId, newParentId, newGoalId)
    }
}
