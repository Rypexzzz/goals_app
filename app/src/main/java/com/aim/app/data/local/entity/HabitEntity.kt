package com.aim.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "habits",
    foreignKeys = [
        ForeignKey(
            entity = GoalEntity::class,
            parentColumns = ["id"],
            childColumns = ["goal_id"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index("goal_id"),
        Index("deleted_at"),
        Index("archived_at"),
        Index("order_index"),
    ],
)
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "goal_id") val goalId: Long?,
    val title: String,
    val description: String?,
    val emoji: String?,
    val frequency: String, // JSON HabitFrequency
    @ColumnInfo(name = "order_index") val orderIndex: Int,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "archived_at") val archivedAt: Long?,
    @ColumnInfo(name = "deleted_at") val deletedAt: Long?,
)
