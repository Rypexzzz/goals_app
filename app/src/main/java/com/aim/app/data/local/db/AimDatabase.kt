package com.aim.app.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.aim.app.data.local.entity.GoalEntity
import com.aim.app.data.local.entity.HabitCheckInEntity
import com.aim.app.data.local.entity.HabitEntity
import com.aim.app.data.local.entity.TaskEntity

@Database(
    entities = [
        GoalEntity::class,
        TaskEntity::class,
        HabitEntity::class,
        HabitCheckInEntity::class,
    ],
    version = AimDatabase.VERSION,
    exportSchema = true,
)
@TypeConverters(AimTypeConverters::class)
abstract class AimDatabase : RoomDatabase() {

    abstract fun goalDao(): GoalDao
    abstract fun taskDao(): TaskDao
    abstract fun habitDao(): HabitDao

    companion object {
        const val VERSION = 2
        const val NAME = "aim.db"
    }
}
