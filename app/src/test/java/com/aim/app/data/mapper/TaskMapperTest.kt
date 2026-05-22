package com.aim.app.data.mapper

import com.aim.app.domain.model.Recurrence
import com.aim.app.domain.model.Task
import com.aim.app.domain.model.TaskStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

class TaskMapperTest {

    @Test
    fun `roundtrip preserves all fields including recurrence`() {
        val task = Task(
            id = 11,
            goalId = 1,
            parentTaskId = 5,
            title = "Тренировка",
            description = "Силовая",
            emoji = "🏋️",
            deadline = LocalDate.of(2026, 12, 31),
            scheduledFor = LocalDate.of(2026, 5, 22),
            scheduledTime = LocalTime.of(7, 30),
            status = TaskStatus.IN_PROGRESS,
            depth = 1,
            orderIndex = 2,
            recurrence = Recurrence.WeeklyOn(setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY)),
            createdAt = Instant.parse("2026-01-01T10:00:00Z"),
            completedAt = null,
            deletedAt = null,
        )

        val back = task.toEntity().toDomain()
        assertEquals(task, back)
    }

    @Test
    fun `null recurrence roundtrips as null`() {
        val task = Task(
            id = 1, goalId = 1, parentTaskId = null,
            title = "T", description = null, emoji = null,
            deadline = null, scheduledFor = null, scheduledTime = null,
            status = TaskStatus.COMPLETED, depth = 0, orderIndex = 0,
            recurrence = null,
            createdAt = Instant.EPOCH, completedAt = Instant.EPOCH, deletedAt = null,
        )
        val entity = task.toEntity()
        assertNull(entity.recurrence)
        assertEquals(task, entity.toDomain())
    }

    @Test
    fun `scheduledTime is encoded as seconds of day`() {
        val task = Task(
            id = 1, goalId = 1, parentTaskId = null,
            title = "T", description = null, emoji = null,
            deadline = null, scheduledFor = null,
            scheduledTime = LocalTime.of(8, 45, 30),
            status = TaskStatus.IN_PROGRESS, depth = 0, orderIndex = 0,
            recurrence = null,
            createdAt = Instant.EPOCH, completedAt = null, deletedAt = null,
        )
        val entity = task.toEntity()
        assertNotNull(entity.scheduledTime)
        assertEquals(8 * 3600 + 45 * 60 + 30, entity.scheduledTime)
        assertEquals(task.scheduledTime, entity.toDomain().scheduledTime)
    }

    @Test
    fun `corrupt recurrence JSON returns null instead of crashing`() {
        val task = Task(
            id = 1, goalId = 1, parentTaskId = null,
            title = "T", description = null, emoji = null,
            deadline = null, scheduledFor = null, scheduledTime = null,
            status = TaskStatus.IN_PROGRESS, depth = 0, orderIndex = 0,
            recurrence = null,
            createdAt = Instant.EPOCH, completedAt = null, deletedAt = null,
        )
        val brokenEntity = task.toEntity().copy(recurrence = "this is not json")
        assertNull(brokenEntity.toDomain().recurrence)
    }
}
