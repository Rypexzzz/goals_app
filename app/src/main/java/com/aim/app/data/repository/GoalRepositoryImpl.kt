package com.aim.app.data.repository

import com.aim.app.data.local.db.GoalDao
import com.aim.app.data.mapper.toDomain
import com.aim.app.data.mapper.toEntity
import com.aim.app.domain.model.Goal
import com.aim.app.domain.model.GoalFilter
import com.aim.app.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoalRepositoryImpl @Inject constructor(
    private val dao: GoalDao,
    private val clock: () -> Instant = Instant::now,
) : GoalRepository {

    override fun observeGoals(filter: GoalFilter): Flow<List<Goal>> {
        val source = when (filter) {
            GoalFilter.ALL -> dao.observeNotDeleted()
            GoalFilter.ACTIVE -> dao.observeActive()
            GoalFilter.COMPLETED -> dao.observeCompleted()
            GoalFilter.ARCHIVED -> dao.observeArchived()
        }
        return source.map { list -> list.map { it.toDomain() } }
    }

    override fun observeAllIncludingDeleted(): Flow<List<Goal>> =
        dao.observeEverything().map { list -> list.map { it.toDomain() } }

    override fun observeGoal(id: Long): Flow<Goal?> =
        dao.observeById(id).map { it?.toDomain() }

    override suspend fun createGoal(goal: Goal): Long {
        val nextOrder = dao.maxOrderIndex() + 1
        val entity = goal.copy(orderIndex = nextOrder).toEntity().copy(id = 0)
        return dao.insert(entity)
    }

    override suspend fun updateGoal(goal: Goal) {
        dao.update(goal.toEntity())
    }

    override suspend fun softDelete(goalId: Long) {
        dao.markDeleted(goalId, clock().toEpochMilli())
    }

    override suspend fun restoreFromTrash(goalId: Long) {
        dao.clearDeleted(goalId)
    }

    override suspend fun archive(goalId: Long) {
        dao.markArchived(goalId, clock().toEpochMilli())
    }

    override suspend fun unarchive(goalId: Long) {
        dao.clearArchived(goalId)
    }

    override suspend fun markCompleted(goalId: Long) {
        dao.markCompleted(goalId, clock().toEpochMilli())
    }

    override suspend fun markInProgress(goalId: Long) {
        dao.markInProgress(goalId)
    }

    override suspend fun reorder(orderedIds: List<Long>) {
        orderedIds.forEachIndexed { index, id -> dao.updateOrderIndex(id, index) }
    }

    override suspend fun permanentlyDelete(goalId: Long) {
        dao.deleteById(goalId)
    }

    override suspend fun purgeDeletedBefore(threshold: java.time.Instant): Int =
        dao.purgeDeletedBefore(threshold.toEpochMilli())
}
