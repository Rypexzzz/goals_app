package com.aim.app.domain.model

import kotlinx.serialization.Serializable
import java.time.DayOfWeek

/**
 * Шаблон повторения задачи. Хранится в БД как JSON-строка (см. [com.aim.app.data.local.db.AimTypeConverters]).
 * Материализация occurrence'ов на конкретные даты — отдельная таблица в Спринте 4.
 */
@Serializable
sealed class Recurrence {

    @Serializable
    data object Daily : Recurrence()

    /** Каждую неделю — фиксированные дни. */
    @Serializable
    data class WeeklyOn(val days: Set<DayOfWeek>) : Recurrence()

    /** Раз в неделю — без привязки к дню. */
    @Serializable
    data object Weekly : Recurrence()

    /** Каждые N дней. N ≥ 1. */
    @Serializable
    data class EveryNDays(val n: Int) : Recurrence() {
        init { require(n >= 1) { "EveryNDays expects n ≥ 1, got $n" } }
    }

    /** Раз в месяц. */
    @Serializable
    data object Monthly : Recurrence()
}
