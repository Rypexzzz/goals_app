package com.aim.app.data.mapper

import com.aim.app.domain.model.Habit
import com.aim.app.domain.model.HabitFrequency
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.DayOfWeek
import java.time.Instant

class HabitMapperTest {

    @Test
    fun `daily habit roundtrip preserves all fields`() {
        val habit = Habit(
            id = 7,
            goalId = 3,
            title = "Не курить",
            description = "Markdown _support_",
            emoji = "🚭",
            frequency = HabitFrequency.Daily,
            orderIndex = 2,
            createdAt = Instant.parse("2026-01-01T10:00:00Z"),
            archivedAt = null,
            deletedAt = null,
        )
        assertEquals(habit, habit.toEntity().toDomain())
    }

    @Test
    fun `specific-days habit preserves day set`() {
        val habit = Habit(
            id = 1,
            goalId = null,
            title = "Тренировка",
            description = null,
            emoji = null,
            frequency = HabitFrequency.SpecificDays(
                setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
            ),
            orderIndex = 0,
            createdAt = Instant.EPOCH,
            archivedAt = null,
            deletedAt = null,
        )
        assertEquals(habit, habit.toEntity().toDomain())
    }

    @Test
    fun `times-per-week habit preserves count`() {
        val habit = Habit(
            id = 1,
            goalId = null,
            title = "Кардио",
            description = null,
            emoji = "🏃",
            frequency = HabitFrequency.TimesPerWeek(times = 4),
            orderIndex = 0,
            createdAt = Instant.EPOCH,
            archivedAt = null,
            deletedAt = null,
        )
        assertEquals(habit, habit.toEntity().toDomain())
    }

    @Test
    fun `corrupt frequency JSON falls back to Daily`() {
        val good = Habit(
            id = 1, goalId = null, title = "T", description = null, emoji = null,
            frequency = HabitFrequency.Daily, orderIndex = 0,
            createdAt = Instant.EPOCH, archivedAt = null, deletedAt = null,
        )
        val broken = good.toEntity().copy(frequency = "not valid json")
        assertEquals(HabitFrequency.Daily, broken.toDomain().frequency)
    }
}
