package com.aim.app.data.repository.backup

import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BackupSerializationTest {

    private val json = Json { prettyPrint = true; encodeDefaults = true; ignoreUnknownKeys = true }

    @Test
    fun `envelope roundtrips through json preserving all entities`() {
        val envelope = BackupEnvelope(
            exportedAt = 1_700_000_000_000L,
            goals = listOf(
                GoalDto(
                    id = 1, title = "Цель", description = "desc", emoji = "🎯",
                    deadline = "2026-12-31", status = "IN_PROGRESS", orderIndex = 0,
                    createdAt = 1L, completedAt = null, archivedAt = null, deletedAt = null,
                ),
            ),
            tasks = listOf(
                TaskDto(
                    id = 2, goalId = 1, parentTaskId = null, title = "Задача", description = null,
                    emoji = null, deadline = null, scheduledFor = "2026-05-22", scheduledTime = 540,
                    status = "IN_PROGRESS", depth = 0, orderIndex = 0,
                    recurrence = """{"type":"com.aim.app.domain.model.Recurrence.Daily"}""",
                    createdAt = 1L, completedAt = null, deletedAt = null,
                ),
            ),
            occurrences = listOf(
                OccurrenceDto(id = 3, taskId = 2, date = "2026-05-22", status = "COMPLETED", completedAt = 5L),
            ),
            habits = listOf(
                HabitDto(
                    id = 4, goalId = 1, title = "Привычка", description = null, emoji = "🚭",
                    frequency = """{"type":"com.aim.app.domain.model.HabitFrequency.Daily"}""",
                    orderIndex = 0, createdAt = 1L, archivedAt = null, deletedAt = null,
                ),
            ),
            checkIns = listOf(
                CheckInDto(id = 5, habitId = 4, date = "2026-05-22", status = "DONE", checkedAt = 6L),
            ),
        )

        val encoded = json.encodeToString(BackupEnvelope.serializer(), envelope)
        val decoded = json.decodeFromString(BackupEnvelope.serializer(), encoded)

        assertEquals(envelope, decoded)
    }

    @Test
    fun `unknown future fields are ignored`() {
        val raw = """
            {
              "version": 1,
              "exportedAt": 1,
              "futureField": "ignored",
              "goals": [],
              "tasks": [],
              "occurrences": [],
              "habits": [],
              "checkIns": []
            }
        """.trimIndent()
        val decoded = json.decodeFromString(BackupEnvelope.serializer(), raw)
        assertEquals(1, decoded.version)
        assertEquals(0, decoded.goals.size)
    }
}
