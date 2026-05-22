package com.aim.app.domain.usecase.task

import com.aim.app.domain.model.Task
import com.aim.app.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveTaskUseCase @Inject constructor(
    private val repository: TaskRepository,
) {
    operator fun invoke(taskId: Long): Flow<Task?> = repository.observeTask(taskId)
}
