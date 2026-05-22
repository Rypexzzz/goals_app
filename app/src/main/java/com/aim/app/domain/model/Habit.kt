package com.aim.app.domain.model

import java.time.Instant

data class Habit(
    val id: Long,
    val goalId: Long?,
    val title: String,
    val description: String?,
    val emoji: String?,
    val frequency: HabitFrequency,
    val orderIndex: Int,
    val createdAt: Instant,
    val archivedAt: Instant?,
    val deletedAt: Instant?,
) {
    val isActive: Boolean get() = deletedAt == null && archivedAt == null
    val isInTrash: Boolean get() = deletedAt != null
    val isArchived: Boolean get() = deletedAt == null && archivedAt != null
}
