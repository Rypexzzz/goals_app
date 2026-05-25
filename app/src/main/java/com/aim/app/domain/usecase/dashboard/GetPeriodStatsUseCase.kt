package com.aim.app.domain.usecase.dashboard

import com.aim.app.domain.model.CheckInStatus
import com.aim.app.domain.model.DashboardPeriod
import com.aim.app.domain.model.PeriodStats
import com.aim.app.domain.repository.HabitRepository
import com.aim.app.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class GetPeriodStatsUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
    private val habitRepository: HabitRepository,
) {
    operator fun invoke(
        period: DashboardPeriod,
        today: LocalDate = LocalDate.now(),
        zone: ZoneId = ZoneId.systemDefault(),
    ): Flow<PeriodStats> {
        val start = when (period) {
            DashboardPeriod.WEEK -> today.minusDays(6)
            DashboardPeriod.MONTH -> today.minusDays(29)
            DashboardPeriod.YEAR -> today.minusDays(364)
        }
        val startInstant = start.atStartOfDay(zone).toInstant()
        val endInstant = today.plusDays(1).atStartOfDay(zone).toInstant().minusMillis(1)

        return combine(
            taskRepository.observeTasksCompletedBetween(startInstant, endInstant),
            taskRepository.observeOccurrencesInRange(start, today),
            habitRepository.observeAllCheckIns(),
        ) { completedTasks, occurrences, checkIns ->
            val productivity = mutableMapOf<LocalDate, Int>()

            completedTasks.forEach { task ->
                task.completedAt?.let { instant ->
                    val day = instant.atZone(zone).toLocalDate()
                    productivity[day] = (productivity[day] ?: 0) + 1
                }
            }
            val completedOccurrences = occurrences.filter { it.isCompleted }
            completedOccurrences.forEach { occ ->
                productivity[occ.date] = (productivity[occ.date] ?: 0) + 1
            }

            val habitsInRange = checkIns.filter { !it.date.isBefore(start) && !it.date.isAfter(today) }
            val done = habitsInRange.count { it.status == CheckInStatus.DONE }
            val failed = habitsInRange.count { it.status == CheckInStatus.FAILED }
            habitsInRange.filter { it.status == CheckInStatus.DONE }.forEach {
                productivity[it.date] = (productivity[it.date] ?: 0) + 1
            }

            val tasksCompleted = completedTasks.size + completedOccurrences.size
            val best = productivity.values.maxOrNull() ?: 0
            val average = if (productivity.isEmpty()) 0f else productivity.values.sum().toFloat() / productivity.size

            PeriodStats(
                period = period,
                tasksCompleted = tasksCompleted,
                habitDone = done,
                habitFailed = failed,
                bestDayCount = best,
                averagePerActiveDay = average,
                productivityByDate = productivity,
            )
        }
    }
}
