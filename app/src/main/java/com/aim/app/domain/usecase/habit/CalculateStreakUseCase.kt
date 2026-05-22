package com.aim.app.domain.usecase.habit

import com.aim.app.domain.model.CheckInStatus
import com.aim.app.domain.model.HabitCheckIn
import com.aim.app.domain.model.HabitFrequency
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import javax.inject.Inject

/**
 * Расчёт текущего и лучшего стрика для всех вариантов [HabitFrequency].
 *
 * Контракт по README §6.3:
 * - **Daily** — последовательность дней с `DONE`, прерывается только при `FAILED`;
 *   отсутствие отметки **не** прерывает стрик.
 * - **TimesPerWeek(n)** — последовательность недель, в каждой из которых ≥ n отметок `DONE`.
 *   Неделя считается «провальной» только если в ней ≥1 `FAILED` И &lt;n `DONE`.
 * - **TimesPerMonth(n)** — аналогично, по месяцам.
 * - **SpecificDays** — последовательность недель, где **все** указанные дни отмечены `DONE`.
 *   Если хотя бы один из заданных дней пропущен или `FAILED` — неделя провальная.
 *
 * Для всех типов «незавершённый» период (текущая неделя/месяц, идущая в будущее)
 * считается частью стрика, пока он не провален «прошедшими» отметками.
 */
class CalculateStreakUseCase @Inject constructor() {

    operator fun invoke(
        frequency: HabitFrequency,
        checkIns: List<HabitCheckIn>,
        today: LocalDate = LocalDate.now(),
        firstDayOfWeek: DayOfWeek = DayOfWeek.MONDAY,
    ): StreakResult {
        if (checkIns.isEmpty()) return StreakResult(current = 0, best = 0)

        val byDate: Map<LocalDate, CheckInStatus> = checkIns.associate { it.date to it.status }
        return when (frequency) {
            HabitFrequency.Daily -> dailyStreak(byDate, today)
            is HabitFrequency.TimesPerWeek -> weeklyStreak(byDate, today, firstDayOfWeek, frequency.times)
            is HabitFrequency.TimesPerMonth -> monthlyStreak(byDate, today, frequency.times)
            is HabitFrequency.SpecificDays -> specificDaysStreak(byDate, today, firstDayOfWeek, frequency.days)
        }
    }

    // -----------------------------------------------------------------------------
    // Daily
    // -----------------------------------------------------------------------------

    private fun dailyStreak(
        byDate: Map<LocalDate, CheckInStatus>,
        today: LocalDate,
    ): StreakResult {
        val firstDate = byDate.keys.min()
        val current = run {
            var count = 0
            var cursor = today
            while (!cursor.isBefore(firstDate)) {
                val status = byDate[cursor]
                when (status) {
                    CheckInStatus.FAILED -> return@run count
                    CheckInStatus.DONE -> count += 1
                    null -> Unit // miss — don't break, don't add
                }
                cursor = cursor.minusDays(1)
            }
            count
        }
        val best = run {
            var best = 0
            var run = 0
            var cursor = firstDate
            while (!cursor.isAfter(today)) {
                when (byDate[cursor]) {
                    CheckInStatus.DONE -> run += 1
                    CheckInStatus.FAILED -> { if (run > best) best = run; run = 0 }
                    null -> Unit
                }
                cursor = cursor.plusDays(1)
            }
            if (run > best) best = run
            best
        }
        return StreakResult(current = current, best = best)
    }

    // -----------------------------------------------------------------------------
    // TimesPerWeek(n)
    // -----------------------------------------------------------------------------

    private fun weeklyStreak(
        byDate: Map<LocalDate, CheckInStatus>,
        today: LocalDate,
        firstDayOfWeek: DayOfWeek,
        n: Int,
    ): StreakResult {
        val firstDate = byDate.keys.min()
        val current = run {
            var count = 0
            var weekStart = today.startOfWeek(firstDayOfWeek)
            while (!weekStart.isBefore(firstDate.startOfWeek(firstDayOfWeek))) {
                val (done, failed) = countWeek(byDate, weekStart)
                val isBroken = failed >= 1 && done < n
                if (isBroken) return@run count
                if (done == 0 && failed == 0 && weekStart.isBefore(today.startOfWeek(firstDayOfWeek))) {
                    // прошлая неделя без единой отметки — формально не «провалена», но и не «выполнена».
                    // Прерываем стрик: иначе стрик может тянуться через бесконечные «пустые» недели.
                    return@run count
                }
                count += 1
                weekStart = weekStart.minusDays(7)
            }
            count
        }
        val best = run {
            var best = 0
            var run = 0
            var weekStart = firstDate.startOfWeek(firstDayOfWeek)
            while (!weekStart.isAfter(today.startOfWeek(firstDayOfWeek))) {
                val (done, failed) = countWeek(byDate, weekStart)
                val isBroken = failed >= 1 && done < n
                val isEmpty = done == 0 && failed == 0
                if (isBroken || isEmpty) {
                    if (run > best) best = run
                    run = 0
                } else {
                    run += 1
                }
                weekStart = weekStart.plusDays(7)
            }
            if (run > best) best = run
            best
        }
        return StreakResult(current = current, best = best)
    }

    // -----------------------------------------------------------------------------
    // TimesPerMonth(n)
    // -----------------------------------------------------------------------------

    private fun monthlyStreak(
        byDate: Map<LocalDate, CheckInStatus>,
        today: LocalDate,
        n: Int,
    ): StreakResult {
        val firstDate = byDate.keys.min()
        val current = run {
            var count = 0
            var month = YearMonth.from(today)
            val earliestMonth = YearMonth.from(firstDate)
            while (!month.isBefore(earliestMonth)) {
                val (done, failed) = countMonth(byDate, month)
                val isBroken = failed >= 1 && done < n
                if (isBroken) return@run count
                if (done == 0 && failed == 0 && month.isBefore(YearMonth.from(today))) {
                    return@run count
                }
                count += 1
                month = month.minusMonths(1)
            }
            count
        }
        val best = run {
            var best = 0
            var run = 0
            var month = YearMonth.from(firstDate)
            val nowMonth = YearMonth.from(today)
            while (!month.isAfter(nowMonth)) {
                val (done, failed) = countMonth(byDate, month)
                val isBroken = failed >= 1 && done < n
                val isEmpty = done == 0 && failed == 0
                if (isBroken || isEmpty) {
                    if (run > best) best = run
                    run = 0
                } else {
                    run += 1
                }
                month = month.plusMonths(1)
            }
            if (run > best) best = run
            best
        }
        return StreakResult(current = current, best = best)
    }

    // -----------------------------------------------------------------------------
    // SpecificDays(days)
    // -----------------------------------------------------------------------------

    private fun specificDaysStreak(
        byDate: Map<LocalDate, CheckInStatus>,
        today: LocalDate,
        firstDayOfWeek: DayOfWeek,
        days: Set<DayOfWeek>,
    ): StreakResult {
        val firstDate = byDate.keys.min()
        fun evaluateWeek(weekStart: LocalDate, allowFuture: Boolean): WeekVerdict {
            var failedOrMissing = 0
            var done = 0
            days.forEach { dow ->
                val date = weekStart.plusDays(((dow.value - firstDayOfWeek.value + 7) % 7).toLong())
                if (date.isAfter(today)) {
                    if (!allowFuture) failedOrMissing += 1
                    return@forEach
                }
                when (byDate[date]) {
                    CheckInStatus.DONE -> done += 1
                    CheckInStatus.FAILED -> failedOrMissing += 1
                    null -> failedOrMissing += 1
                }
            }
            return WeekVerdict(success = failedOrMissing == 0 && done > 0, hasAnyEntry = done > 0)
        }

        val current = run {
            var count = 0
            var weekStart = today.startOfWeek(firstDayOfWeek)
            val firstWeekStart = firstDate.startOfWeek(firstDayOfWeek)
            while (!weekStart.isBefore(firstWeekStart)) {
                val isCurrentWeek = weekStart == today.startOfWeek(firstDayOfWeek)
                val verdict = evaluateWeek(weekStart, allowFuture = isCurrentWeek)
                if (!verdict.success) {
                    // Для текущей недели — может быть «ещё всё впереди» (done=0, missing=0 для будущего).
                    // Считаем, что стрик продолжается, если ни одного провала.
                    if (isCurrentWeek && verdict.hasAnyEntry.not()) {
                        // ещё не начали — не прерываем, но и не считаем
                    } else if (isCurrentWeek && verdict.hasAnyEntry) {
                        // частично выполнено в текущей неделе и нет провалов → +1
                        count += 1
                    } else {
                        return@run count
                    }
                } else {
                    count += 1
                }
                weekStart = weekStart.minusDays(7)
            }
            count
        }
        val best = run {
            var best = 0
            var run = 0
            var weekStart = firstDate.startOfWeek(firstDayOfWeek)
            val nowWeekStart = today.startOfWeek(firstDayOfWeek)
            while (!weekStart.isAfter(nowWeekStart)) {
                val isCurrent = weekStart == nowWeekStart
                val verdict = evaluateWeek(weekStart, allowFuture = isCurrent)
                if (verdict.success) {
                    run += 1
                } else {
                    if (run > best) best = run
                    run = 0
                }
                weekStart = weekStart.plusDays(7)
            }
            if (run > best) best = run
            best
        }
        return StreakResult(current = current, best = best)
    }

    // -----------------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------------

    private fun countWeek(
        byDate: Map<LocalDate, CheckInStatus>,
        weekStart: LocalDate,
    ): Pair<Int, Int> {
        var done = 0
        var failed = 0
        repeat(7) { offset ->
            when (byDate[weekStart.plusDays(offset.toLong())]) {
                CheckInStatus.DONE -> done += 1
                CheckInStatus.FAILED -> failed += 1
                null -> Unit
            }
        }
        return done to failed
    }

    private fun countMonth(
        byDate: Map<LocalDate, CheckInStatus>,
        month: YearMonth,
    ): Pair<Int, Int> {
        var done = 0
        var failed = 0
        val daysInMonth = month.lengthOfMonth()
        for (day in 1..daysInMonth) {
            when (byDate[month.atDay(day)]) {
                CheckInStatus.DONE -> done += 1
                CheckInStatus.FAILED -> failed += 1
                null -> Unit
            }
        }
        return done to failed
    }

    private fun LocalDate.startOfWeek(firstDayOfWeek: DayOfWeek): LocalDate {
        val daysFromStart = ((this.dayOfWeek.value - firstDayOfWeek.value + 7) % 7).toLong()
        return this.minusDays(daysFromStart)
    }

    data class StreakResult(val current: Int, val best: Int)
    private data class WeekVerdict(val success: Boolean, val hasAnyEntry: Boolean)
}

/** Чисто-доменная утилита: количество дней между двумя датами включительно. */
internal fun daysInclusive(start: LocalDate, end: LocalDate): Long =
    ChronoUnit.DAYS.between(start, end) + 1
