package com.aim.app.domain.usecase.dashboard

import com.aim.app.domain.model.HabitHeatmap
import com.aim.app.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

class GetHabitHeatmapsUseCase @Inject constructor(
    private val habitRepository: HabitRepository,
) {
    operator fun invoke(
        today: LocalDate = LocalDate.now(),
        weeks: Int = DEFAULT_WEEKS,
        firstDayOfWeek: DayOfWeek = DayOfWeek.MONDAY,
    ): Flow<List<HabitHeatmap>> =
        combine(
            habitRepository.observeActiveHabits(),
            habitRepository.observeAllCheckIns(),
        ) { habits, checkIns ->
            val start = today
                .with(TemporalAdjusters.previousOrSame(firstDayOfWeek))
                .minusWeeks((weeks - 1).toLong())
            val byHabit = checkIns.groupBy { it.habitId }
            habits.map { habit ->
                val statuses = byHabit[habit.id].orEmpty()
                    .filter { !it.date.isBefore(start) && !it.date.isAfter(today) }
                    .associate { it.date to it.status }
                HabitHeatmap(habit = habit, statusByDate = statuses)
            }
        }

    companion object {
        const val DEFAULT_WEEKS = 13
    }
}
