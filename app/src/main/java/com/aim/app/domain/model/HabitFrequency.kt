package com.aim.app.domain.model

import com.aim.app.domain.serialization.DayOfWeekSerializer
import kotlinx.serialization.Serializable
import java.time.DayOfWeek

/**
 * Частота отметки привычки. Сериализуется как JSON-строка в `habits.frequency`
 * (см. [com.aim.app.data.local.db.AimTypeConverters]).
 */
@Serializable
sealed class HabitFrequency {

    /** Каждый день. */
    @Serializable
    data object Daily : HabitFrequency()

    /** N произвольных дней в неделе. N ≥ 1. */
    @Serializable
    data class TimesPerWeek(val times: Int) : HabitFrequency() {
        init { require(times in 1..7) { "TimesPerWeek expects 1..7, got $times" } }
    }

    /** N произвольных дней в месяце. N ≥ 1. */
    @Serializable
    data class TimesPerMonth(val times: Int) : HabitFrequency() {
        init { require(times in 1..31) { "TimesPerMonth expects 1..31, got $times" } }
    }

    /** Конкретные дни недели (например, Пн/Ср/Пт). */
    @Serializable
    data class SpecificDays(
        val days: Set<@Serializable(with = DayOfWeekSerializer::class) DayOfWeek>,
    ) : HabitFrequency() {
        init { require(days.isNotEmpty()) { "SpecificDays expects at least one day" } }
    }
}
