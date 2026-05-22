package com.aim.app.data.mapper

import com.aim.app.data.local.entity.TaskEntity
import com.aim.app.domain.model.Recurrence
import com.aim.app.domain.model.Task
import com.aim.app.domain.model.TaskStatus
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

private val recurrenceJson = Json { ignoreUnknownKeys = true }

fun TaskEntity.toDomain(): Task = Task(
    id = id,
    goalId = goalId,
    parentTaskId = parentTaskId,
    title = title,
    description = description,
    emoji = emoji,
    deadline = deadline?.let(LocalDate::parse),
    scheduledFor = scheduledFor?.let(LocalDate::parse),
    scheduledTime = scheduledTime?.let { LocalTime.ofSecondOfDay(it.toLong()) },
    status = runCatching { TaskStatus.valueOf(status) }.getOrDefault(TaskStatus.IN_PROGRESS),
    depth = depth,
    orderIndex = orderIndex,
    recurrence = recurrence?.let {
        runCatching { recurrenceJson.decodeFromString(Recurrence.serializer(), it) }.getOrNull()
    },
    createdAt = Instant.ofEpochMilli(createdAt),
    completedAt = completedAt?.let(Instant::ofEpochMilli),
    deletedAt = deletedAt?.let(Instant::ofEpochMilli),
)

fun Task.toEntity(): TaskEntity = TaskEntity(
    id = id,
    goalId = goalId,
    parentTaskId = parentTaskId,
    title = title,
    description = description,
    emoji = emoji,
    deadline = deadline?.toString(),
    scheduledFor = scheduledFor?.toString(),
    scheduledTime = scheduledTime?.toSecondOfDay(),
    status = status.name,
    depth = depth,
    orderIndex = orderIndex,
    recurrence = recurrence?.let { recurrenceJson.encodeToString(Recurrence.serializer(), it) },
    createdAt = createdAt.toEpochMilli(),
    completedAt = completedAt?.toEpochMilli(),
    deletedAt = deletedAt?.toEpochMilli(),
)
