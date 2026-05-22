package com.aim.app.domain.usecase.habit

import com.aim.app.domain.model.Habit
import com.aim.app.domain.model.HabitFrequency
import com.aim.app.domain.repository.HabitRepository
import java.time.Instant
import javax.inject.Inject

class CreateHabitUseCase @Inject constructor(
    private val repository: HabitRepository,
    private val clock: () -> Instant = Instant::now,
) {
    suspend operator fun invoke(
        title: String,
        description: String?,
        emoji: String?,
        frequency: HabitFrequency,
        goalId: Long?,
    ): Long {
        val trimmed = title.trim()
        require(trimmed.isNotEmpty()) { "Habit title must not be blank" }

        val draft = Habit(
            id = 0,
            goalId = goalId,
            title = trimmed,
            description = description?.takeIf { it.isNotBlank() },
            emoji = emoji?.takeIf { it.isNotBlank() },
            frequency = frequency,
            orderIndex = 0,
            createdAt = clock(),
            archivedAt = null,
            deletedAt = null,
        )
        return repository.createHabit(draft)
    }
}
