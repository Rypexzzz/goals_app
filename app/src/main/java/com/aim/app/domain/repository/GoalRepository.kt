package com.aim.app.domain.repository

import com.aim.app.domain.model.Goal
import com.aim.app.domain.model.GoalFilter
import kotlinx.coroutines.flow.Flow

interface GoalRepository {

    fun observeGoals(filter: GoalFilter): Flow<List<Goal>>

    /** Полный snapshot — нужен для агрегации корзины (узнать заголовки целей, к которым относятся задачи). */
    fun observeAllIncludingDeleted(): Flow<List<Goal>>

    fun observeGoal(id: Long): Flow<Goal?>

    suspend fun createGoal(goal: Goal): Long

    suspend fun updateGoal(goal: Goal)

    suspend fun softDelete(goalId: Long)

    suspend fun restoreFromTrash(goalId: Long)

    suspend fun archive(goalId: Long)

    suspend fun unarchive(goalId: Long)

    suspend fun markCompleted(goalId: Long)

    suspend fun markInProgress(goalId: Long)

    suspend fun reorder(orderedIds: List<Long>)

    suspend fun permanentlyDelete(goalId: Long)

    /** Окончательно удалить цели в корзине, удалённые раньше [threshold]. Возвращает число удалённых. */
    suspend fun purgeDeletedBefore(threshold: java.time.Instant): Int
}
