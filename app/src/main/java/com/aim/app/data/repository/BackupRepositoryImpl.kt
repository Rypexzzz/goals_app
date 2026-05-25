package com.aim.app.data.repository

import androidx.room.withTransaction
import com.aim.app.data.local.db.AimDatabase
import com.aim.app.data.local.db.GoalDao
import com.aim.app.data.local.db.HabitDao
import com.aim.app.data.local.db.TaskDao
import com.aim.app.data.local.db.TaskOccurrenceDao
import com.aim.app.data.local.entity.GoalEntity
import com.aim.app.data.local.entity.HabitCheckInEntity
import com.aim.app.data.local.entity.HabitEntity
import com.aim.app.data.local.entity.TaskEntity
import com.aim.app.data.local.entity.TaskOccurrenceEntity
import com.aim.app.data.repository.backup.BackupEnvelope
import com.aim.app.data.repository.backup.CheckInDto
import com.aim.app.data.repository.backup.GoalDto
import com.aim.app.data.repository.backup.HabitDto
import com.aim.app.data.repository.backup.OccurrenceDto
import com.aim.app.data.repository.backup.TaskDto
import com.aim.app.domain.repository.BackupRepository
import kotlinx.serialization.json.Json
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupRepositoryImpl @Inject constructor(
    private val database: AimDatabase,
    private val goalDao: GoalDao,
    private val taskDao: TaskDao,
    private val occurrenceDao: TaskOccurrenceDao,
    private val habitDao: HabitDao,
    private val clock: () -> Instant = Instant::now,
) : BackupRepository {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override suspend fun exportToJson(): String {
        val envelope = BackupEnvelope(
            exportedAt = clock().toEpochMilli(),
            goals = goalDao.getAllOnce().map { it.toDto() },
            tasks = taskDao.getAllOnce().map { it.toDto() },
            occurrences = occurrenceDao.getAllOnce().map { it.toDto() },
            habits = habitDao.getAllHabitsOnce().map { it.toDto() },
            checkIns = habitDao.getAllCheckInsOnce().map { it.toDto() },
        )
        return json.encodeToString(BackupEnvelope.serializer(), envelope)
    }

    override suspend fun importFromJson(json: String) {
        val envelope = this.json.decodeFromString(BackupEnvelope.serializer(), json)
        database.withTransaction {
            // Очистка в обратном FK-порядке.
            habitDao.clearCheckIns()
            habitDao.clearHabits()
            occurrenceDao.clear()
            taskDao.clear()
            goalDao.clear()
            // Вставка в прямом FK-порядке.
            goalDao.insertAll(envelope.goals.map { it.toEntity() })
            taskDao.insertAll(envelope.tasks.map { it.toEntity() })
            occurrenceDao.insertAll(envelope.occurrences.map { it.toEntity() })
            habitDao.insertAllHabits(envelope.habits.map { it.toEntity() })
            habitDao.insertAllCheckIns(envelope.checkIns.map { it.toEntity() })
        }
    }
}

// --- entity ↔ dto ---

private fun GoalEntity.toDto() = GoalDto(id, title, description, emoji, deadline, status, orderIndex, createdAt, completedAt, archivedAt, deletedAt)
private fun GoalDto.toEntity() = GoalEntity(id, title, description, emoji, deadline, status, orderIndex, createdAt, completedAt, archivedAt, deletedAt)

private fun TaskEntity.toDto() = TaskDto(id, goalId, parentTaskId, title, description, emoji, deadline, scheduledFor, scheduledTime, status, depth, orderIndex, recurrence, createdAt, completedAt, deletedAt)
private fun TaskDto.toEntity() = TaskEntity(id, goalId, parentTaskId, title, description, emoji, deadline, scheduledFor, scheduledTime, status, depth, orderIndex, recurrence, createdAt, completedAt, deletedAt)

private fun TaskOccurrenceEntity.toDto() = OccurrenceDto(id, taskId, date, status, completedAt)
private fun OccurrenceDto.toEntity() = TaskOccurrenceEntity(id, taskId, date, status, completedAt)

private fun HabitEntity.toDto() = HabitDto(id, goalId, title, description, emoji, frequency, orderIndex, createdAt, archivedAt, deletedAt)
private fun HabitDto.toEntity() = HabitEntity(id, goalId, title, description, emoji, frequency, orderIndex, createdAt, archivedAt, deletedAt)

private fun HabitCheckInEntity.toDto() = CheckInDto(id, habitId, date, status, checkedAt)
private fun CheckInDto.toEntity() = HabitCheckInEntity(id, habitId, date, status, checkedAt)
