package com.aim.app.domain.model

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

data class Task(
    val id: Long,
    val goalId: Long,
    val parentTaskId: Long?,
    val title: String,
    val description: String?,
    val emoji: String?,
    val deadline: LocalDate?,
    val scheduledFor: LocalDate?,
    val scheduledTime: LocalTime?,
    val status: TaskStatus,
    val depth: Int,
    val orderIndex: Int,
    val recurrence: Recurrence?,
    val createdAt: Instant,
    val completedAt: Instant?,
    val deletedAt: Instant?,
) {
    companion object {
        /** UI-ограничение: 5 уровней задач под целью (depth ∈ 0..4). */
        const val MAX_DEPTH: Int = 4
    }

    val isCompleted: Boolean get() = status == TaskStatus.COMPLETED
    val isInTrash: Boolean get() = deletedAt != null
    val canHaveSubtasks: Boolean get() = depth < MAX_DEPTH
}
