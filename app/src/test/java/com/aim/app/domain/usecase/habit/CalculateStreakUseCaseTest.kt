package com.aim.app.domain.usecase.habit

import com.aim.app.domain.model.CheckInStatus
import com.aim.app.domain.model.HabitCheckIn
import com.aim.app.domain.model.HabitFrequency
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate

class CalculateStreakUseCaseTest {

    private val sut = CalculateStreakUseCase()
    private val today = LocalDate.of(2026, 5, 22) // Friday

    // Хелпер для краткости.
    private fun ci(date: LocalDate, status: CheckInStatus) =
        HabitCheckIn(id = 0, habitId = 1, date = date, status = status, checkedAt = Instant.EPOCH)

    @Nested
    inner class Daily {

        @Test
        fun `empty check-ins yield zero`() {
            val res = sut(HabitFrequency.Daily, emptyList(), today)
            assertEquals(0, res.current)
            assertEquals(0, res.best)
        }

        @Test
        fun `three consecutive DONE days ending today gives streak of 3`() {
            val checkIns = (0..2).map { ci(today.minusDays(it.toLong()), CheckInStatus.DONE) }
            val res = sut(HabitFrequency.Daily, checkIns, today)
            assertEquals(3, res.current)
            assertEquals(3, res.best)
        }

        @Test
        fun `missing days in the middle do not break streak`() {
            val checkIns = listOf(
                ci(today, CheckInStatus.DONE),
                // missing today-1
                ci(today.minusDays(2), CheckInStatus.DONE),
                ci(today.minusDays(3), CheckInStatus.DONE),
            )
            val res = sut(HabitFrequency.Daily, checkIns, today)
            assertEquals(3, res.current)
            assertEquals(3, res.best)
        }

        @Test
        fun `FAILED day breaks streak`() {
            val checkIns = listOf(
                ci(today, CheckInStatus.DONE),
                ci(today.minusDays(1), CheckInStatus.DONE),
                ci(today.minusDays(2), CheckInStatus.FAILED),
                ci(today.minusDays(3), CheckInStatus.DONE),
            )
            val res = sut(HabitFrequency.Daily, checkIns, today)
            assertEquals(2, res.current) // today + today-1
            assertEquals(2, res.best) // before FAILED: just today-3 (1); after: 2
        }

        @Test
        fun `today FAILED yields zero current but preserves best`() {
            val checkIns = listOf(
                ci(today, CheckInStatus.FAILED),
                ci(today.minusDays(1), CheckInStatus.DONE),
                ci(today.minusDays(2), CheckInStatus.DONE),
                ci(today.minusDays(3), CheckInStatus.DONE),
            )
            val res = sut(HabitFrequency.Daily, checkIns, today)
            assertEquals(0, res.current)
            assertEquals(3, res.best)
        }

        @Test
        fun `best streak across multiple FAILED is the longest DONE run`() {
            // Pattern: DDD F DDDDD F D
            val days = listOf(
                CheckInStatus.DONE, CheckInStatus.DONE, CheckInStatus.DONE,
                CheckInStatus.FAILED,
                CheckInStatus.DONE, CheckInStatus.DONE, CheckInStatus.DONE, CheckInStatus.DONE, CheckInStatus.DONE,
                CheckInStatus.FAILED,
                CheckInStatus.DONE,
            )
            val checkIns = days.mapIndexed { i, s ->
                ci(today.minusDays((days.size - 1 - i).toLong()), s)
            }
            val res = sut(HabitFrequency.Daily, checkIns, today)
            assertEquals(1, res.current) // last day before today, plus today itself
            assertEquals(5, res.best)
        }
    }

    @Nested
    inner class TimesPerWeek {

        @Test
        fun `four DONE days in current week gives streak 1 for n=3`() {
            val mon = today.minusDays(((today.dayOfWeek.value - DayOfWeek.MONDAY.value + 7) % 7).toLong())
            val checkIns = listOf(
                ci(mon, CheckInStatus.DONE),
                ci(mon.plusDays(1), CheckInStatus.DONE),
                ci(mon.plusDays(2), CheckInStatus.DONE),
                ci(mon.plusDays(3), CheckInStatus.DONE),
            )
            val res = sut(HabitFrequency.TimesPerWeek(times = 3), checkIns, today)
            assertEquals(1, res.current)
        }

        @Test
        fun `current week with only one DONE for n=3 and no FAILED still counts as part of streak`() {
            val mon = today.minusDays(((today.dayOfWeek.value - DayOfWeek.MONDAY.value + 7) % 7).toLong())
            val checkIns = listOf(
                ci(mon, CheckInStatus.DONE),
            )
            val res = sut(HabitFrequency.TimesPerWeek(times = 3), checkIns, today)
            // нет FAILED → неделя «успешна» по строгому правилу
            assertEquals(1, res.current)
        }

        @Test
        fun `week with FAILED and less than n DONE breaks streak`() {
            val mon = today.minusDays(((today.dayOfWeek.value - DayOfWeek.MONDAY.value + 7) % 7).toLong())
            val checkIns = listOf(
                ci(mon, CheckInStatus.DONE),
                ci(mon.plusDays(1), CheckInStatus.FAILED),
            )
            val res = sut(HabitFrequency.TimesPerWeek(times = 3), checkIns, today)
            assertEquals(0, res.current)
        }

        @Test
        fun `week with FAILED but n DONE still counts as success`() {
            val mon = today.minusDays(((today.dayOfWeek.value - DayOfWeek.MONDAY.value + 7) % 7).toLong())
            val checkIns = listOf(
                ci(mon, CheckInStatus.DONE),
                ci(mon.plusDays(1), CheckInStatus.DONE),
                ci(mon.plusDays(2), CheckInStatus.DONE),
                ci(mon.plusDays(3), CheckInStatus.FAILED),
            )
            val res = sut(HabitFrequency.TimesPerWeek(times = 3), checkIns, today)
            assertEquals(1, res.current)
        }
    }

    @Nested
    inner class TimesPerMonth {

        @Test
        fun `current month with enough DONE for n=10 gives streak 1`() {
            val firstOfMonth = today.withDayOfMonth(1)
            val checkIns = (0..9).map { ci(firstOfMonth.plusDays(it.toLong()), CheckInStatus.DONE) }
            val res = sut(HabitFrequency.TimesPerMonth(times = 10), checkIns, today)
            assertEquals(1, res.current)
        }

        @Test
        fun `month with FAILED and less than n DONE breaks streak`() {
            val firstOfMonth = today.withDayOfMonth(1)
            val checkIns = listOf(
                ci(firstOfMonth, CheckInStatus.DONE),
                ci(firstOfMonth.plusDays(1), CheckInStatus.FAILED),
            )
            val res = sut(HabitFrequency.TimesPerMonth(times = 5), checkIns, today)
            assertEquals(0, res.current)
        }
    }

    @Nested
    inner class SpecificDays {

        @Test
        fun `all required days marked DONE in current week gives streak 1`() {
            // today = Friday (2026-05-22). Required: Mon, Wed, Fri.
            val mon = today.minusDays(4)
            val wed = today.minusDays(2)
            val fri = today
            val checkIns = listOf(
                ci(mon, CheckInStatus.DONE),
                ci(wed, CheckInStatus.DONE),
                ci(fri, CheckInStatus.DONE),
            )
            val res = sut(
                HabitFrequency.SpecificDays(setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)),
                checkIns,
                today,
            )
            assertEquals(1, res.current)
        }

        @Test
        fun `missing one required day in past week breaks streak`() {
            val lastMon = today.minusDays((today.dayOfWeek.value - 1 + 7).toLong())
            val lastWed = lastMon.plusDays(2)
            // Fri missing
            val checkIns = listOf(
                ci(lastMon, CheckInStatus.DONE),
                ci(lastWed, CheckInStatus.DONE),
            )
            val res = sut(
                HabitFrequency.SpecificDays(setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)),
                checkIns,
                today,
            )
            assertEquals(0, res.current)
        }
    }
}
