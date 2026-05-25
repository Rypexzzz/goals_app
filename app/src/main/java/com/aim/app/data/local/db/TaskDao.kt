package com.aim.app.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.aim.app.data.local.db.projection.GoalTaskCount
import com.aim.app.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(task: TaskEntity): Long

    @Update
    suspend fun update(task: TaskEntity)

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getById(id: Long): TaskEntity?

    @Query("SELECT * FROM tasks WHERE id = :id")
    fun observeById(id: Long): Flow<TaskEntity?>

    // В SQLite NULL по умолчанию сортируется раньше любого значения при ASC — это даёт нам
    // корни (parent_task_id IS NULL) первыми, без явного `NULLS FIRST` (его Room-парсер не понимает).
    // Финальная иерархия и порядок строятся в `ObserveTasksForGoalUseCase.buildForest()`.
    @Query(
        """
        SELECT * FROM tasks
        WHERE goal_id = :goalId AND deleted_at IS NULL
        ORDER BY parent_task_id ASC, order_index ASC
        """,
    )
    fun observeForGoal(goalId: Long): Flow<List<TaskEntity>>

    @Query(
        """
        SELECT t.* FROM tasks t
        INNER JOIN goals g ON g.id = t.goal_id
        WHERE t.deleted_at IS NOT NULL AND g.deleted_at IS NULL
        ORDER BY t.deleted_at DESC
        """,
    )
    fun observeTrashed(): Flow<List<TaskEntity>>

    @Query(
        """
        SELECT COALESCE(MAX(order_index), -1) FROM tasks
        WHERE goal_id = :goalId
          AND (parent_task_id = :parentId OR (parent_task_id IS NULL AND :parentId IS NULL))
          AND deleted_at IS NULL
        """,
    )
    suspend fun maxOrderIndex(goalId: Long, parentId: Long?): Int

    @Query("UPDATE tasks SET order_index = :newOrder WHERE id = :id")
    suspend fun updateOrderIndex(id: Long, newOrder: Int)

    @Query(
        """
        UPDATE tasks SET
            parent_task_id = :newParentId,
            goal_id = :newGoalId,
            depth = :newDepth,
            order_index = :newOrder
        WHERE id = :id
        """,
    )
    suspend fun moveTask(
        id: Long,
        newParentId: Long?,
        newGoalId: Long,
        newDepth: Int,
        newOrder: Int,
    )

    @Query("UPDATE tasks SET depth = :newDepth WHERE id = :id")
    suspend fun updateDepth(id: Long, newDepth: Int)

    @Query("UPDATE tasks SET deleted_at = :now WHERE id = :id")
    suspend fun markDeleted(id: Long, now: Long)

    @Query("UPDATE tasks SET deleted_at = NULL WHERE id = :id")
    suspend fun clearDeleted(id: Long)

    @Query("UPDATE tasks SET status = 'COMPLETED', completed_at = :now WHERE id = :id")
    suspend fun markCompleted(id: Long, now: Long)

    @Query("UPDATE tasks SET status = 'IN_PROGRESS', completed_at = NULL WHERE id = :id")
    suspend fun markInProgress(id: Long)

    @Query("UPDATE tasks SET scheduled_for = :date WHERE id = :id")
    suspend fun updateScheduledFor(id: Long, date: String?)

    /** Разовые (не регулярные) задачи, запланированные на конкретную дату. */
    @Query(
        """
        SELECT t.* FROM tasks t
        INNER JOIN goals g ON g.id = t.goal_id
        WHERE t.scheduled_for = :date
          AND t.recurrence IS NULL
          AND t.deleted_at IS NULL
          AND g.deleted_at IS NULL
        ORDER BY t.scheduled_time ASC, t.order_index ASC
        """,
    )
    fun observeScheduledFor(date: String): Flow<List<TaskEntity>>

    /** Все живые регулярные задачи (с recurrence) — для расчёта экземпляров на лету. */
    @Query(
        """
        SELECT t.* FROM tasks t
        INNER JOIN goals g ON g.id = t.goal_id
        WHERE t.recurrence IS NOT NULL
          AND t.deleted_at IS NULL
          AND g.deleted_at IS NULL
        """,
    )
    fun observeRecurringTasks(): Flow<List<TaskEntity>>

    /** Просроченные разовые задачи: запланированы до :date, не выполнены. */
    @Query(
        """
        SELECT t.* FROM tasks t
        INNER JOIN goals g ON g.id = t.goal_id
        WHERE t.scheduled_for IS NOT NULL
          AND t.scheduled_for < :date
          AND t.recurrence IS NULL
          AND t.status = 'IN_PROGRESS'
          AND t.deleted_at IS NULL
          AND g.deleted_at IS NULL
        ORDER BY t.scheduled_for ASC
        """,
    )
    fun observeOverdue(date: String): Flow<List<TaskEntity>>

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM tasks WHERE deleted_at IS NOT NULL AND deleted_at < :threshold")
    suspend fun purgeDeletedBefore(threshold: Long): Int

    /** Счётчики задач первого уровня по каждой цели — для прогресса на дашборде. */
    @Query(
        """
        SELECT t.goal_id AS goalId,
               COUNT(*) AS total,
               SUM(CASE WHEN t.status = 'COMPLETED' THEN 1 ELSE 0 END) AS done
        FROM tasks t
        WHERE t.parent_task_id IS NULL AND t.deleted_at IS NULL
        GROUP BY t.goal_id
        """,
    )
    fun observeFirstLevelCounts(): Flow<List<GoalTaskCount>>

    /** Разовые задачи, выполненные в диапазоне (по completed_at) — для статистики периода. */
    @Query(
        """
        SELECT * FROM tasks
        WHERE status = 'COMPLETED'
          AND completed_at IS NOT NULL
          AND completed_at >= :startMillis
          AND completed_at <= :endMillis
          AND deleted_at IS NULL
        """,
    )
    fun observeCompletedBetween(startMillis: Long, endMillis: Long): Flow<List<TaskEntity>>

    /**
     * Возвращает идентификаторы поддерева задачи через рекурсивный CTE.
     * Включает саму задачу.
     */
    @Query(
        """
        WITH RECURSIVE subtree(id) AS (
            SELECT id FROM tasks WHERE id = :rootId
            UNION ALL
            SELECT t.id FROM tasks t INNER JOIN subtree s ON t.parent_task_id = s.id
        )
        SELECT id FROM subtree
        """,
    )
    suspend fun getSubtreeIds(rootId: Long): List<Long>

    @Transaction
    suspend fun reorderInParent(orderedIds: List<Long>) {
        orderedIds.forEachIndexed { index, id -> updateOrderIndex(id, index) }
    }
}
