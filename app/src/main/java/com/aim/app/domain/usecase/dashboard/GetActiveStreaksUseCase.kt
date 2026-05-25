package com.aim.app.domain.usecase.dashboard

import com.aim.app.domain.model.StreakEntry
import com.aim.app.domain.repository.HabitRepository
import com.aim.app.domain.usecase.habit.CalculateStreakUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import javax.inject.Inject

class GetActiveStreaksUseCase @Inject constructor(
    private val habitRepository: HabitRepository,
    private val calculateStreak: CalculateStreakUseCase,
) {
    operator fun invoke(today: LocalDate = LocalDate.now()): Flow<List<StreakEntry>> =
        combine(
            habitRepository.observeActiveHabits(),
            habitRepository.observeAllCheckIns(),
        ) { habits, checkIns ->
            val byHabit = checkIns.groupBy { it.habitId }
            habits.map { habit ->
                val streak = calculateStreak(habit.frequency, byHabit[habit.id].orEmpty(), today)
                StreakEntry(habit = habit, currentStreak = streak.current, bestStreak = streak.best)
            }.sortedByDescending { it.currentStreak }
        }
}
