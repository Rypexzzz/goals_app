package com.aim.app.domain.usecase.today

import com.aim.app.domain.model.CheckInStatus
import com.aim.app.domain.model.HabitCheckIn
import com.aim.app.domain.model.HabitFrequency
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate

class HabitSchedulerTest {

    private val today = LocalDate.of(2026, 5, 22) // Friday

    private fun ci(date: LocalDate, status: CheckInStatus = CheckInStatus.DONE) =
        HabitCheckIn(id = 0, habitId = 1, date = date, status = status, checkedAt = Instant.EPOCH)

    @Test
    fun `daily always due`() {
        assertTrue(HabitScheduler.isDueOn(HabitFrequency.Daily, today, emptyList()))
    }

    @Test
    fun `specific days due only on listed weekday`() {
        val friOnly = HabitFrequency.SpecificDays(setOf(DayOfWeek.FRIDAY))
        assertTrue(HabitScheduler.isDueOn(friOnly, today, emptyList()))
        val monOnly = HabitFrequency.SpecificDays(setOf(DayOfWeek.MONDAY))
        assertFalse(HabitScheduler.isDueOn(monOnly, today, emptyList()))
    }

    @Test
    fun `times per week due while under quota`() {
        val freq = HabitFrequency.TimesPerWeek(times = 3)
        val mon = today.minusDays(4)
        // 2 done this week, quota 3 → still due
        val checkIns = listOf(ci(mon), ci(mon.plusDays(1)))
        assertTrue(HabitScheduler.isDueOn(freq, today, checkIns))
    }

    @Test
    fun `times per week not due once quota reached and not acted today`() {
        val freq = HabitFrequency.TimesPerWeek(times = 3)
        val mon = today.minusDays(4)
        val checkIns = listOf(ci(mon), ci(mon.plusDays(1)), ci(mon.plusDays(2)))
        assertFalse(HabitScheduler.isDueOn(freq, today, checkIns))
    }

    @Test
    fun `times per week still shown if acted today even past quota`() {
        val freq = HabitFrequency.TimesPerWeek(times = 3)
        val mon = today.minusDays(4)
        val checkIns = listOf(
            ci(mon), ci(mon.plusDays(1)), ci(mon.plusDays(2)),
            ci(today), // acted today
        )
        assertTrue(HabitScheduler.isDueOn(freq, today, checkIns))
    }

    @Test
    fun `times per month due while under quota`() {
        val freq = HabitFrequency.TimesPerMonth(times = 5)
        val firstOfMonth = today.withDayOfMonth(1)
        val checkIns = (0..2).map { ci(firstOfMonth.plusDays(it.toLong())) }
        assertTrue(HabitScheduler.isDueOn(freq, today, checkIns))
    }
}
