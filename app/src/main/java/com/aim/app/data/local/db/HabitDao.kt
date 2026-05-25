package com.aim.app.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.aim.app.data.local.entity.HabitCheckInEntity
import com.aim.app.data.local.entity.HabitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {

    // -----------------------------------------------------------------------------
    // Habits
    // -----------------------------------------------------------------------------

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(habit: HabitEntity): Long

    @Update
    suspend fun update(habit: HabitEntity)

    @Query(
        """
        SELECT * FROM habits
        WHERE deleted_at IS NULL AND archived_at IS NULL
        ORDER BY order_index ASC, created_at DESC
        """,
    )
    fun observeActive(): Flow<List<HabitEntity>>

    @Query(
        """
        SELECT * FROM habits
        WHERE deleted_at IS NULL AND archived_at IS NOT NULL
        ORDER BY archived_at DESC
        """,
    )
    fun observeArchived(): Flow<List<HabitEntity>>

    @Query(
        """
        SELECT * FROM habits
        WHERE deleted_at IS NOT NULL
        ORDER BY deleted_at DESC
        """,
    )
    fun observeTrashed(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits")
    fun observeEverything(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE id = :id")
    fun observeById(id: Long): Flow<HabitEntity?>

    @Query(
        """
        SELECT * FROM habits
        WHERE goal_id = :goalId AND deleted_at IS NULL AND archived_at IS NULL
        ORDER BY order_index ASC, created_at DESC
        """,
    )
    fun observeForGoal(goalId: Long): Flow<List<HabitEntity>>

    @Query(
        """
        SELECT COALESCE(MAX(order_index), -1) FROM habits
        WHERE deleted_at IS NULL AND archived_at IS NULL
        """,
    )
    suspend fun maxOrderIndex(): Int

    @Query("UPDATE habits SET order_index = :newOrder WHERE id = :id")
    suspend fun updateOrderIndex(id: Long, newOrder: Int)

    @Query("UPDATE habits SET deleted_at = :now, archived_at = NULL WHERE id = :id")
    suspend fun markDeleted(id: Long, now: Long)

    @Query("UPDATE habits SET deleted_at = NULL WHERE id = :id")
    suspend fun clearDeleted(id: Long)

    @Query("UPDATE habits SET archived_at = :now WHERE id = :id")
    suspend fun markArchived(id: Long, now: Long)

    @Query("UPDATE habits SET archived_at = NULL WHERE id = :id")
    suspend fun clearArchived(id: Long)

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM habits WHERE deleted_at IS NOT NULL AND deleted_at < :threshold")
    suspend fun purgeDeletedBefore(threshold: Long): Int

    // -----------------------------------------------------------------------------
    // Check-ins
    // -----------------------------------------------------------------------------

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCheckIn(entry: HabitCheckInEntity): Long

    @Query("DELETE FROM habit_check_ins WHERE habit_id = :habitId AND date = :date")
    suspend fun deleteCheckIn(habitId: Long, date: String)

    @Query(
        """
        SELECT * FROM habit_check_ins
        WHERE habit_id = :habitId
        ORDER BY date ASC
        """,
    )
    fun observeCheckIns(habitId: Long): Flow<List<HabitCheckInEntity>>

    @Query(
        """
        SELECT * FROM habit_check_ins
        WHERE habit_id = :habitId
          AND date >= :startInclusive
          AND date <= :endInclusive
        ORDER BY date ASC
        """,
    )
    fun observeCheckInsInRange(
        habitId: Long,
        startInclusive: String,
        endInclusive: String,
    ): Flow<List<HabitCheckInEntity>>

    @Query("SELECT * FROM habit_check_ins WHERE habit_id = :habitId AND date = :date")
    suspend fun getCheckIn(habitId: Long, date: String): HabitCheckInEntity?

    /** Все отметки всех привычек — для экрана «Сегодня» (расчёт due/done и стриков). */
    @Query("SELECT * FROM habit_check_ins ORDER BY date ASC")
    fun observeAllCheckIns(): Flow<List<HabitCheckInEntity>>
}
