package com.aim.app.domain.usecase.goal

import com.aim.app.domain.model.Goal
import com.aim.app.domain.model.GoalStatus
import com.aim.app.domain.repository.GoalRepository
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject

class CreateGoalUseCase @Inject constructor(
    private val repository: GoalRepository,
    private val clock: () -> Instant = Instant::now,
) {
    suspend operator fun invoke(
        title: String,
        description: String?,
        emoji: String?,
        deadline: LocalDate?,
    ): Long {
        val trimmedTitle = title.trim()
        require(trimmedTitle.isNotEmpty()) { "Goal title must not be blank" }

        val now = clock()
        val draft = Goal(
            id = 0,
            title = trimmedTitle,
            description = description?.takeIf { it.isNotBlank() },
            emoji = emoji?.takeIf { it.isNotBlank() },
            deadline = deadline,
            status = GoalStatus.IN_PROGRESS,
            orderIndex = 0,
            createdAt = now,
            completedAt = null,
            archivedAt = null,
            deletedAt = null,
        )
        return repository.createGoal(draft)
    }
}
