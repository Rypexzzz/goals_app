package com.aim.app.domain.usecase.habit

import com.aim.app.domain.model.CheckInStatus
import com.aim.app.domain.model.Goal
import com.aim.app.domain.model.Habit
import com.aim.app.domain.model.HabitCheckIn
import com.aim.app.domain.model.HabitStats
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

/**
 * Чистая трансформация (Habit, [Goal?], List<HabitCheckIn>) → [HabitStats].
 * Без обращения к репозиторию — ViewModel сам собирает потоки и вызывает.
 */
class GetHabitStatsUseCase @Inject constructor(
    private val calculateStreak: CalculateStreakUseCase,
) {
    operator fun invoke(
        habit: Habit,
        linkedGoal: Goal?,
        checkIns: List<HabitCheckIn>,
        today: LocalDate = LocalDate.now(),
    ): HabitStats {
        val streak = calculateStreak(habit.frequency, checkIns, today)
        val done = checkIns.count { it.status == CheckInStatus.DONE }
        val failed = checkIns.count { it.status == CheckInStatus.FAILED }

        val percent: Float? = computeCompletion(
            habit = habit,
            linkedGoal = linkedGoal,
            doneCount = done,
            today = today,
        )

        return HabitStats(
            currentStreak = streak.current,
            bestStreak = streak.best,
            totalDone = done,
            totalFailed = failed,
            completionPercent = percent,
        )
    }

    private fun computeCompletion(
        habit: Habit,
        linkedGoal: Goal?,
        doneCount: Int,
        today: LocalDate,
    ): Float? {
        val deadline = linkedGoal?.deadline ?: return null
        val createdDate = habit.createdAt.atZone(java.time.ZoneId.systemDefault()).toLocalDate()
        val expectedDays = ChronoUnit.DAYS.between(createdDate, deadline) + 1
        if (expectedDays <= 0) return null
        val ratio = doneCount.toFloat() / expectedDays.toFloat()
        return ratio.coerceIn(0f, 1f) * 100f
    }
}
