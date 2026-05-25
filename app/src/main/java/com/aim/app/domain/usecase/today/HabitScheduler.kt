package com.aim.app.domain.usecase.today

import com.aim.app.domain.model.CheckInStatus
import com.aim.app.domain.model.HabitCheckIn
import com.aim.app.domain.model.HabitFrequency
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

/**
 * Чистая логика «актуальна ли привычка на сегодня» (README §6.4).
 *
 * - **Daily** — всегда.
 * - **SpecificDays(days)** — если сегодня входит в days.
 * - **TimesPerWeek(n)** — пока за текущую неделю выполнено < n (либо уже есть отметка за сегодня).
 * - **TimesPerMonth(n)** — пока за текущий месяц выполнено < n (либо уже есть отметка за сегодня).
 */
object HabitScheduler {

    fun isDueOn(
        frequency: HabitFrequency,
        date: LocalDate,
        checkIns: List<HabitCheckIn>,
        firstDayOfWeek: DayOfWeek = DayOfWeek.MONDAY,
    ): Boolean {
        val hasEntryToday = checkIns.any { it.date == date }
        return when (frequency) {
            HabitFrequency.Daily -> true
            is HabitFrequency.SpecificDays -> date.dayOfWeek in frequency.days
            is HabitFrequency.TimesPerWeek -> {
                val doneThisWeek = countDone(checkIns) { sameWeek(it, date, firstDayOfWeek) }
                doneThisWeek < frequency.times || hasEntryToday
            }
            is HabitFrequency.TimesPerMonth -> {
                val doneThisMonth = countDone(checkIns) { YearMonth.from(it) == YearMonth.from(date) }
                doneThisMonth < frequency.times || hasEntryToday
            }
        }
    }

    private inline fun countDone(
        checkIns: List<HabitCheckIn>,
        predicate: (LocalDate) -> Boolean,
    ): Int = checkIns.count { it.status == CheckInStatus.DONE && predicate(it.date) }

    private fun sameWeek(a: LocalDate, b: LocalDate, firstDayOfWeek: DayOfWeek): Boolean =
        startOfWeek(a, firstDayOfWeek) == startOfWeek(b, firstDayOfWeek)

    private fun startOfWeek(date: LocalDate, firstDayOfWeek: DayOfWeek): LocalDate {
        val shift = ((date.dayOfWeek.value - firstDayOfWeek.value + 7) % 7).toLong()
        return date.minusDays(shift)
    }
}
