package com.aim.app.domain.model

/**
 * Типы уведомлений (README §8.1). Часть — по расписанию (WorkManager),
 * часть — точные алармы (AlarmManager).
 */
enum class NotificationType {
    MORNING_BRIEF,        // 08:00 — сводка дня
    FIRST_THING,          // 09:00 — приоритетная задача
    EVENING_CHECKIN,      // 21:30 — отметить привычки
    STREAK_WARNING,       // 22:30 — стрик под угрозой
    WEEKLY_SUMMARY,       // вс 20:00 — итог недели
    DEADLINE_APPROACHING, // дедлайн цели близко
    ;

    val isTimed: Boolean get() = this != DEADLINE_APPROACHING
}
