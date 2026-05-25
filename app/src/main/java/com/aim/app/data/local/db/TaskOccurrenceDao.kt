package com.aim.app.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aim.app.data.local.entity.TaskOccurrenceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskOccurrenceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(occurrence: TaskOccurrenceEntity): Long

    @Query("DELETE FROM task_occurrences WHERE task_id = :taskId AND date = :date")
    suspend fun delete(taskId: Long, date: String)

    @Query("SELECT * FROM task_occurrences WHERE task_id = :taskId AND date = :date")
    suspend fun getOccurrence(taskId: Long, date: String): TaskOccurrenceEntity?

    @Query(
        """
        SELECT * FROM task_occurrences
        WHERE date >= :startInclusive AND date <= :endInclusive
        """,
    )
    fun observeInRange(
        startInclusive: String,
        endInclusive: String,
    ): Flow<List<TaskOccurrenceEntity>>

    @Query("SELECT * FROM task_occurrences WHERE task_id = :taskId ORDER BY date ASC")
    fun observeForTask(taskId: Long): Flow<List<TaskOccurrenceEntity>>

    @Query("SELECT * FROM task_occurrences")
    suspend fun getAllOnce(): List<TaskOccurrenceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(occurrences: List<TaskOccurrenceEntity>)

    @Query("DELETE FROM task_occurrences")
    suspend fun clear()
}
