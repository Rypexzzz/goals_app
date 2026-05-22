package com.aim.app.data.mapper

import com.aim.app.domain.model.Goal
import com.aim.app.domain.model.GoalStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate

class GoalMapperTest {

    @Test
    fun `roundtrip preserves all fields`() {
        val goal = Goal(
            id = 7,
            title = "Стать сильнее",
            description = "Markdown _description_",
            emoji = "💪",
            deadline = LocalDate.of(2026, 12, 31),
            status = GoalStatus.IN_PROGRESS,
            orderIndex = 3,
            createdAt = Instant.parse("2026-01-01T10:00:00Z"),
            completedAt = null,
            archivedAt = null,
            deletedAt = null,
        )

        val roundtripped = goal.toEntity().toDomain()
        assertEquals(goal, roundtripped)
    }

    @Test
    fun `null deadline maps to null and back`() {
        val goal = Goal(
            id = 1,
            title = "T",
            description = null,
            emoji = null,
            deadline = null,
            status = GoalStatus.COMPLETED,
            orderIndex = 0,
            createdAt = Instant.parse("2026-05-01T00:00:00Z"),
            completedAt = Instant.parse("2026-05-15T00:00:00Z"),
            archivedAt = Instant.parse("2026-05-16T00:00:00Z"),
            deletedAt = null,
        )

        val entity = goal.toEntity()
        assertNull(entity.deadline)

        val back = entity.toDomain()
        assertEquals(goal, back)
    }

    @Test
    fun `unknown status string falls back to IN_PROGRESS`() {
        val goal = Goal(
            id = 1, title = "T", description = null, emoji = null, deadline = null,
            status = GoalStatus.IN_PROGRESS, orderIndex = 0,
            createdAt = Instant.EPOCH, completedAt = null, archivedAt = null, deletedAt = null,
        )
        val brokenEntity = goal.toEntity().copy(status = "GARBAGE_VALUE")
        val recovered = brokenEntity.toDomain()
        assertEquals(GoalStatus.IN_PROGRESS, recovered.status)
    }
}
