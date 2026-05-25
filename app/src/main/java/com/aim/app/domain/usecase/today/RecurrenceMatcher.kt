package com.aim.app.domain.usecase.today

import com.aim.app.domain.model.Recurrence
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Чистая проверка: должна ли регулярная задача с заданным [Recurrence] иметь экземпляр
 * на дату [date], если «якорная» дата (старт серии) — [anchorDate].
 *
 * - **Daily** — каждый день.
 * - **WeeklyOn(days)** — если день недели входит в набор.
 * - **Weekly** — раз в неделю, в тот же день недели, что и якорь.
 * - **EveryNDays(n)** — каждый n-й день от якоря (включая сам якорь).
 * - **Monthly** — раз в месяц, в тот же день месяца, что и якорь
 *   (если в месяце нет такого числа — берётся последний день месяца).
 */
object RecurrenceMatcher {

    fun occursOn(recurrence: Recurrence, date: LocalDate, anchorDate: LocalDate): Boolean {
        if (date.isBefore(anchorDate)) return false
        return when (recurrence) {
            Recurrence.Daily -> true
            is Recurrence.WeeklyOn -> date.dayOfWeek in recurrence.days
            Recurrence.Weekly -> date.dayOfWeek == anchorDate.dayOfWeek
            is Recurrence.EveryNDays -> {
                val diff = ChronoUnit.DAYS.between(anchorDate, date)
                diff % recurrence.n == 0L
            }
            Recurrence.Monthly -> {
                val targetDay = minOf(anchorDate.dayOfMonth, date.lengthOfMonth())
                date.dayOfMonth == targetDay
            }
        }
    }
}
