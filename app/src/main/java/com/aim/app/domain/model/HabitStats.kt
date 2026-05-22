package com.aim.app.domain.model

data class HabitStats(
    val currentStreak: Int,
    val bestStreak: Int,
    val totalDone: Int,
    val totalFailed: Int,
    /**
     * Процент выполнения от создания привычки до дедлайна привязанной цели.
     * null, если привычка не привязана к цели или у цели нет дедлайна.
     */
    val completionPercent: Float?,
)
