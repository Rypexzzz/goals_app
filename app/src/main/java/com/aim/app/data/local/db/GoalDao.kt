package com.aim.app.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.aim.app.data.local.entity.GoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(goal: GoalEntity): Long

    @Update
    suspend fun update(goal: GoalEntity)

    /** Все цели вне корзины — то, что показывается на вкладке «Все». */
    @Query(
        """
        SELECT * FROM goals
        WHERE deleted_at IS NULL
        ORDER BY order_index ASC, created_at DESC
        """,
    )
    fun observeNotDeleted(): Flow<List<GoalEntity>>

    @Query(
        """
        SELECT * FROM goals
        WHERE deleted_at IS NULL AND archived_at IS NULL AND status = 'IN_PROGRESS'
        ORDER BY order_index ASC, created_at DESC
        """,
    )
    fun observeActive(): Flow<List<GoalEntity>>

    @Query(
        """
        SELECT * FROM goals
        WHERE deleted_at IS NULL AND archived_at IS NULL AND status = 'COMPLETED'
        ORDER BY completed_at DESC
        """,
    )
    fun observeCompleted(): Flow<List<GoalEntity>>

    @Query(
        """
        SELECT * FROM goals
        WHERE deleted_at IS NULL AND archived_at IS NOT NULL
        ORDER BY archived_at DESC
        """,
    )
    fun observeArchived(): Flow<List<GoalEntity>>

    @Query(
        """
        SELECT * FROM goals
        WHERE deleted_at IS NOT NULL
        ORDER BY deleted_at DESC
        """,
    )
    fun observeTrashed(): Flow<List<GoalEntity>>

    /** Полный snapshot — нужен `ObserveTrashUseCase` для подтягивания заголовков целей к задачам. */
    @Query("SELECT * FROM goals")
    fun observeEverything(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE id = :id")
    fun observeById(id: Long): Flow<GoalEntity?>

    @Query("SELECT * FROM goals WHERE id = :id")
    suspend fun getById(id: Long): GoalEntity?

    @Query(
        """
        SELECT COALESCE(MAX(order_index), -1) FROM goals
        WHERE deleted_at IS NULL AND archived_at IS NULL
        """,
    )
    suspend fun maxOrderIndex(): Int

    @Query("UPDATE goals SET order_index = :newOrder WHERE id = :id")
    suspend fun updateOrderIndex(id: Long, newOrder: Int)

    @Query(
        """
        UPDATE goals SET
            deleted_at = :now,
            archived_at = NULL
        WHERE id = :id
        """,
    )
    suspend fun markDeleted(id: Long, now: Long)

    @Query("UPDATE goals SET deleted_at = NULL WHERE id = :id")
    suspend fun clearDeleted(id: Long)

    @Query("UPDATE goals SET archived_at = :now WHERE id = :id")
    suspend fun markArchived(id: Long, now: Long)

    @Query("UPDATE goals SET archived_at = NULL WHERE id = :id")
    suspend fun clearArchived(id: Long)

    @Query(
        """
        UPDATE goals SET
            status = 'COMPLETED',
            completed_at = :now
        WHERE id = :id
        """,
    )
    suspend fun markCompleted(id: Long, now: Long)

    @Query(
        """
        UPDATE goals SET
            status = 'IN_PROGRESS',
            completed_at = NULL
        WHERE id = :id
        """,
    )
    suspend fun markInProgress(id: Long)

    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM goals WHERE deleted_at IS NOT NULL AND deleted_at < :threshold")
    suspend fun purgeDeletedBefore(threshold: Long): Int

    @Query("SELECT * FROM goals")
    suspend fun getAllOnce(): List<GoalEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(goals: List<GoalEntity>)

    @Query("DELETE FROM goals")
    suspend fun clear()
}
