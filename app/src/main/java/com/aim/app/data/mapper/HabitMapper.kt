package com.aim.app.data.mapper

import com.aim.app.data.local.entity.HabitEntity
import com.aim.app.domain.model.Habit
import com.aim.app.domain.model.HabitFrequency
import kotlinx.serialization.json.Json
import java.time.Instant

private val habitJson = Json { ignoreUnknownKeys = true }

fun HabitEntity.toDomain(): Habit = Habit(
    id = id,
    goalId = goalId,
    title = title,
    description = description,
    emoji = emoji,
    frequency = runCatching {
        habitJson.decodeFromString(HabitFrequency.serializer(), frequency)
    }.getOrDefault(HabitFrequency.Daily),
    orderIndex = orderIndex,
    createdAt = Instant.ofEpochMilli(createdAt),
    archivedAt = archivedAt?.let(Instant::ofEpochMilli),
    deletedAt = deletedAt?.let(Instant::ofEpochMilli),
)

fun Habit.toEntity(): HabitEntity = HabitEntity(
    id = id,
    goalId = goalId,
    title = title,
    description = description,
    emoji = emoji,
    frequency = habitJson.encodeToString(HabitFrequency.serializer(), frequency),
    orderIndex = orderIndex,
    createdAt = createdAt.toEpochMilli(),
    archivedAt = archivedAt?.toEpochMilli(),
    deletedAt = deletedAt?.toEpochMilli(),
)
