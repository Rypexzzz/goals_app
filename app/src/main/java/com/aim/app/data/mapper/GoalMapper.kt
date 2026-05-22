package com.aim.app.data.mapper

import com.aim.app.data.local.entity.GoalEntity
import com.aim.app.domain.model.Goal
import com.aim.app.domain.model.GoalStatus
import java.time.Instant
import java.time.LocalDate

fun GoalEntity.toDomain(): Goal = Goal(
    id = id,
    title = title,
    description = description,
    emoji = emoji,
    deadline = deadline?.let(LocalDate::parse),
    status = runCatching { GoalStatus.valueOf(status) }.getOrDefault(GoalStatus.IN_PROGRESS),
    orderIndex = orderIndex,
    createdAt = Instant.ofEpochMilli(createdAt),
    completedAt = completedAt?.let(Instant::ofEpochMilli),
    archivedAt = archivedAt?.let(Instant::ofEpochMilli),
    deletedAt = deletedAt?.let(Instant::ofEpochMilli),
)

fun Goal.toEntity(): GoalEntity = GoalEntity(
    id = id,
    title = title,
    description = description,
    emoji = emoji,
    deadline = deadline?.toString(),
    status = status.name,
    orderIndex = orderIndex,
    createdAt = createdAt.toEpochMilli(),
    completedAt = completedAt?.toEpochMilli(),
    archivedAt = archivedAt?.toEpochMilli(),
    deletedAt = deletedAt?.toEpochMilli(),
)
