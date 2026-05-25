package com.aim.app.domain.usecase.today

import com.aim.app.domain.model.Recurrence
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.DayOfWeek
import java.time.LocalDate

class RecurrenceMatcherTest {

    private val anchor = LocalDate.of(2026, 5, 1) // Friday

    @Test
    fun `daily occurs every day from anchor`() {
        assertTrue(RecurrenceMatcher.occursOn(Recurrence.Daily, anchor, anchor))
        assertTrue(RecurrenceMatcher.occursOn(Recurrence.Daily, anchor.plusDays(10), anchor))
    }

    @Test
    fun `nothing occurs before anchor`() {
        assertFalse(RecurrenceMatcher.occursOn(Recurrence.Daily, anchor.minusDays(1), anchor))
    }

    @Test
    fun `weeklyOn matches only listed days`() {
        val r = Recurrence.WeeklyOn(setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY))
        val monday = LocalDate.of(2026, 5, 4)
        val tuesday = LocalDate.of(2026, 5, 5)
        val wednesday = LocalDate.of(2026, 5, 6)
        assertTrue(RecurrenceMatcher.occursOn(r, monday, anchor))
        assertFalse(RecurrenceMatcher.occursOn(r, tuesday, anchor))
        assertTrue(RecurrenceMatcher.occursOn(r, wednesday, anchor))
    }

    @Test
    fun `weekly matches same weekday as anchor`() {
        // anchor is Friday
        assertTrue(RecurrenceMatcher.occursOn(Recurrence.Weekly, anchor.plusDays(7), anchor))
        assertFalse(RecurrenceMatcher.occursOn(Recurrence.Weekly, anchor.plusDays(1), anchor))
    }

    @Test
    fun `everyNDays matches multiples of n`() {
        val r = Recurrence.EveryNDays(3)
        assertTrue(RecurrenceMatcher.occursOn(r, anchor, anchor))
        assertFalse(RecurrenceMatcher.occursOn(r, anchor.plusDays(1), anchor))
        assertFalse(RecurrenceMatcher.occursOn(r, anchor.plusDays(2), anchor))
        assertTrue(RecurrenceMatcher.occursOn(r, anchor.plusDays(3), anchor))
        assertTrue(RecurrenceMatcher.occursOn(r, anchor.plusDays(6), anchor))
    }

    @Test
    fun `monthly matches same day-of-month`() {
        // anchor day = 1
        assertTrue(RecurrenceMatcher.occursOn(Recurrence.Monthly, LocalDate.of(2026, 6, 1), anchor))
        assertFalse(RecurrenceMatcher.occursOn(Recurrence.Monthly, LocalDate.of(2026, 6, 2), anchor))
    }

    @Test
    fun `monthly clamps to last day for short months`() {
        val endOfMonthAnchor = LocalDate.of(2026, 1, 31)
        // February 2026 has 28 days → occurs on Feb 28
        assertTrue(RecurrenceMatcher.occursOn(Recurrence.Monthly, LocalDate.of(2026, 2, 28), endOfMonthAnchor))
        assertFalse(RecurrenceMatcher.occursOn(Recurrence.Monthly, LocalDate.of(2026, 2, 27), endOfMonthAnchor))
    }
}
