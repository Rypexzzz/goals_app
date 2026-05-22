package com.aim.app.domain.model

import java.time.Instant
import java.time.LocalDate

data class Goal(
    val id: Long,
    val title: String,
    val description: String?,
    val emoji: String?,
    val deadline: LocalDate?,
    val status: GoalStatus,
    val orderIndex: Int,
    val createdAt: Instant,
    val completedAt: Instant?,
    val archivedAt: Instant?,
    val deletedAt: Instant?,
) {
    val isActive: Boolean get() = deletedAt == null && archivedAt == null && status == GoalStatus.IN_PROGRESS
    val isInTrash: Boolean get() = deletedAt != null
    val isArchived: Boolean get() = deletedAt == null && archivedAt != null
}
