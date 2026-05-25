package com.aim.app.domain.model

import java.time.LocalDate

/** Период для секции статистики дашборда. */
enum class DashboardPeriod { WEEK, MONTH, YEAR }

/** Подсчёт задач первого уровня для цели (из БД, для прогресса). */
data class GoalTaskTally(
    val goalId: Long,
    val total: Int,
    val done: Int,
)

/** Секция «Сводка дня». */
data class DashboardSummary(
    val date: LocalDate,
    val doneToday: Int,
    val totalToday: Int,
    val activeHabitsToday: Int,
) {
    val progress: Float get() = if (totalToday == 0) 0f else doneToday.toFloat() / totalToday
}

/** Карточка активного стрика. */
data class StreakEntry(
    val habit: Habit,
    val currentStreak: Int,
    val bestStreak: Int,
)

/** Тепловая карта одной привычки за последние недели. */
data class HabitHeatmap(
    val habit: Habit,
    val statusByDate: Map<LocalDate, CheckInStatus>,
)

/** Прогресс цели по задачам первого уровня. */
data class GoalProgress(
    val goal: Goal,
    val doneFirstLevel: Int,
    val totalFirstLevel: Int,
) {
    val progress: Float get() = if (totalFirstLevel == 0) 0f else doneFirstLevel.toFloat() / totalFirstLevel
}

/** Статистика за выбранный период. */
data class PeriodStats(
    val period: DashboardPeriod,
    val tasksCompleted: Int,
    val habitDone: Int,
    val habitFailed: Int,
    val bestDayCount: Int,
    val averagePerActiveDay: Float,
    /** Количество выполнений по датам — для тепловой карты продуктивности. */
    val productivityByDate: Map<LocalDate, Int>,
)
