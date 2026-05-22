package com.aim.app.domain.usecase.task

import com.aim.app.domain.model.Recurrence
import com.aim.app.domain.model.Task
import com.aim.app.domain.model.TaskStatus
import com.aim.app.domain.repository.TaskRepository
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

class CreateTaskUseCase @Inject constructor(
    private val repository: TaskRepository,
    private val clock: () -> Instant = Instant::now,
) {
    /**
     * Создаёт задачу под `parentTaskId` (null — задача первого уровня).
     * Поля `depth` и `orderIndex` рассчитывает репозиторий из контекста родителя.
     * Валидация глубины — также на стороне репозитория ([Task.MAX_DEPTH]).
     */
    suspend operator fun invoke(
        goalId: Long,
        parentTaskId: Long?,
        title: String,
        description: String? = null,
        emoji: String? = null,
        deadline: LocalDate? = null,
        scheduledFor: LocalDate? = null,
        scheduledTime: LocalTime? = null,
        recurrence: Recurrence? = null,
    ): Long {
        val trimmed = title.trim()
        require(trimmed.isNotEmpty()) { "Task title must not be blank" }

        val draft = Task(
            id = 0,
            goalId = goalId,
            parentTaskId = parentTaskId,
            title = trimmed,
            description = description?.takeIf { it.isNotBlank() },
            emoji = emoji?.takeIf { it.isNotBlank() },
            deadline = deadline,
            scheduledFor = scheduledFor,
            scheduledTime = scheduledTime,
            status = TaskStatus.IN_PROGRESS,
            depth = 0, // overridden by repository based on parent
            orderIndex = 0, // overridden by repository
            recurrence = recurrence,
            createdAt = clock(),
            completedAt = null,
            deletedAt = null,
        )
        return repository.createTask(draft)
    }
}
