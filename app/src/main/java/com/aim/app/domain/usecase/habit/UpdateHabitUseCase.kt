package com.aim.app.domain.usecase.habit

import com.aim.app.domain.model.Habit
import com.aim.app.domain.repository.HabitRepository
import javax.inject.Inject

class UpdateHabitUseCase @Inject constructor(
    private val repository: HabitRepository,
) {
    suspend operator fun invoke(habit: Habit) {
        val trimmed = habit.title.trim()
        require(trimmed.isNotEmpty()) { "Habit title must not be blank" }
        repository.updateHabit(
            habit.copy(
                title = trimmed,
                description = habit.description?.takeIf { it.isNotBlank() },
                emoji = habit.emoji?.takeIf { it.isNotBlank() },
            ),
        )
    }
}
