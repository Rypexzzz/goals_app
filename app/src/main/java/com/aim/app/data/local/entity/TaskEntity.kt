package com.aim.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = GoalEntity::class,
            parentColumns = ["id"],
            childColumns = ["goal_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["parent_task_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("goal_id"),
        Index("parent_task_id"),
        Index("deleted_at"),
        Index("scheduled_for"),
        Index("order_index"),
    ],
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "goal_id") val goalId: Long,
    @ColumnInfo(name = "parent_task_id") val parentTaskId: Long?,
    val title: String,
    val description: String?,
    val emoji: String?,
    val deadline: String?, // ISO LocalDate
    @ColumnInfo(name = "scheduled_for") val scheduledFor: String?, // ISO LocalDate
    @ColumnInfo(name = "scheduled_time") val scheduledTime: Int?, // seconds of day
    val status: String, // TaskStatus.name
    val depth: Int,
    @ColumnInfo(name = "order_index") val orderIndex: Int,
    val recurrence: String?, // JSON Recurrence
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "completed_at") val completedAt: Long?,
    @ColumnInfo(name = "deleted_at") val deletedAt: Long?,
)
