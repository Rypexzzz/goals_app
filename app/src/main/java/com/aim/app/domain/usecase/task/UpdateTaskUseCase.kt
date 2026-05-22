package com.aim.app.domain.usecase.task

import com.aim.app.domain.model.Task
import com.aim.app.domain.repository.TaskRepository
import javax.inject.Inject

class UpdateTaskUseCase @Inject constructor(
    private val repository: TaskRepository,
) {
    suspend operator fun invoke(task: Task) {
        val trimmed = task.title.trim()
        require(trimmed.isNotEmpty()) { "Task title must not be blank" }
        repository.updateTask(
            task.copy(
                title = trimmed,
                description = task.description?.takeIf { it.isNotBlank() },
                emoji = task.emoji?.takeIf { it.isNotBlank() },
            ),
        )
    }
}
