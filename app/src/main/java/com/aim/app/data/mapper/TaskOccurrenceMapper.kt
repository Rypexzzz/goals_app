package com.aim.app.data.mapper

import com.aim.app.data.local.entity.TaskOccurrenceEntity
import com.aim.app.domain.model.TaskOccurrence
import com.aim.app.domain.model.TaskStatus
import java.time.Instant
import java.time.LocalDate

fun TaskOccurrenceEntity.toDomain(): TaskOccurrence = TaskOccurrence(
    id = id,
    taskId = taskId,
    date = LocalDate.parse(date),
    status = runCatching { TaskStatus.valueOf(status) }.getOrDefault(TaskStatus.IN_PROGRESS),
    completedAt = completedAt?.let(Instant::ofEpochMilli),
)

fun TaskOccurrence.toEntity(): TaskOccurrenceEntity = TaskOccurrenceEntity(
    id = id,
    taskId = taskId,
    date = date.toString(),
    status = status.name,
    completedAt = completedAt?.toEpochMilli(),
)
