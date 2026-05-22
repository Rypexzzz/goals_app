package com.aim.app.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.aim.app.data.local.entity.GoalEntity
import com.aim.app.data.local.entity.TaskEntity

@Database(
    entities = [GoalEntity::class, TaskEntity::class],
    version = AimDatabase.VERSION,
    exportSchema = true,
)
@TypeConverters(AimTypeConverters::class)
abstract class AimDatabase : RoomDatabase() {

    abstract fun goalDao(): GoalDao
    abstract fun taskDao(): TaskDao

    companion object {
        const val VERSION = 1
        const val NAME = "aim.db"
    }
}
