package com.aim.app.domain.usecase.task

import com.aim.app.domain.repository.TaskRepository
import javax.inject.Inject

class UncompleteTaskUseCase @Inject constructor(
    private val repository: TaskRepository,
) {
    suspend operator fun invoke(taskId: Long) = repository.markInProgress(taskId)
}
