package com.aim.app.data.local.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Миграция v2 → v3: таблица `task_occurrences` для материализованных экземпляров
 * регулярных задач. Sprint 4.
 */
val MIGRATION_2_3: Migration = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS task_occurrences (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                task_id INTEGER NOT NULL,
                date TEXT NOT NULL,
                status TEXT NOT NULL,
                completed_at INTEGER,
                FOREIGN KEY(task_id) REFERENCES tasks(id) ON DELETE CASCADE
            )
            """.trimIndent(),
        )
        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS index_task_occurrences_task_id_date ON task_occurrences(task_id, date)",
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_task_occurrences_date ON task_occurrences(date)")
    }
}

/**
 * Миграция v1 → v2: добавление таблиц `habits` и `habit_check_ins`.
 * Sprint 3.
 */
val MIGRATION_1_2: Migration = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS habits (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                goal_id INTEGER,
                title TEXT NOT NULL,
                description TEXT,
                emoji TEXT,
                frequency TEXT NOT NULL,
                order_index INTEGER NOT NULL,
                created_at INTEGER NOT NULL,
                archived_at INTEGER,
                deleted_at INTEGER,
                FOREIGN KEY(goal_id) REFERENCES goals(id) ON DELETE SET NULL
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_habits_goal_id ON habits(goal_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_habits_deleted_at ON habits(deleted_at)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_habits_archived_at ON habits(archived_at)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_habits_order_index ON habits(order_index)")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS habit_check_ins (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                habit_id INTEGER NOT NULL,
                date TEXT NOT NULL,
                status TEXT NOT NULL,
                checked_at INTEGER NOT NULL,
                FOREIGN KEY(habit_id) REFERENCES habits(id) ON DELETE CASCADE
            )
            """.trimIndent(),
        )
        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS index_habit_check_ins_habit_id_date ON habit_check_ins(habit_id, date)",
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_habit_check_ins_date ON habit_check_ins(date)")
    }
}
