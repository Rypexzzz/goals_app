package com.aim.app.data.repository.backup

import kotlinx.serialization.Serializable

/**
 * Сериализуемый снимок всей БД для экспорта/импорта (README §6.8).
 * Поля зеркалят Room-сущности (примитивы), чтобы маппинг был тривиальным и стабильным.
 */
@Serializable
data class BackupEnvelope(
    val version: Int = CURRENT_VERSION,
    val exportedAt: Long,
    val goals: List<GoalDto> = emptyList(),
    val tasks: List<TaskDto> = emptyList(),
    val occurrences: List<OccurrenceDto> = emptyList(),
    val habits: List<HabitDto> = emptyList(),
    val checkIns: List<CheckInDto> = emptyList(),
) {
    companion object {
        const val CURRENT_VERSION = 1
    }
}

@Serializable
data class GoalDto(
    val id: Long,
    val title: String,
    val description: String?,
    val emoji: String?,
    val deadline: String?,
    val status: String,
    val orderIndex: Int,
    val createdAt: Long,
    val completedAt: Long?,
    val archivedAt: Long?,
    val deletedAt: Long?,
)

@Serializable
data class TaskDto(
    val id: Long,
    val goalId: Long,
    val parentTaskId: Long?,
    val title: String,
    val description: String?,
    val emoji: String?,
    val deadline: String?,
    val scheduledFor: String?,
    val scheduledTime: Int?,
    val status: String,
    val depth: Int,
    val orderIndex: Int,
    val recurrence: String?,
    val createdAt: Long,
    val completedAt: Long?,
    val deletedAt: Long?,
)

@Serializable
data class OccurrenceDto(
    val id: Long,
    val taskId: Long,
    val date: String,
    val status: String,
    val completedAt: Long?,
)

@Serializable
data class HabitDto(
    val id: Long,
    val goalId: Long?,
    val title: String,
    val description: String?,
    val emoji: String?,
    val frequency: String,
    val orderIndex: Int,
    val createdAt: Long,
    val archivedAt: Long?,
    val deletedAt: Long?,
)

@Serializable
data class CheckInDto(
    val id: Long,
    val habitId: Long,
    val date: String,
    val status: String,
    val checkedAt: Long,
)
