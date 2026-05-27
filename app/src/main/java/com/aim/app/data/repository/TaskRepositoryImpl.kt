package com.aim.app.data.repository

import androidx.room.withTransaction
import com.aim.app.data.local.db.AimDatabase
import com.aim.app.data.local.db.TaskDao
import com.aim.app.data.local.db.TaskOccurrenceDao
import com.aim.app.data.local.entity.TaskOccurrenceEntity
import com.aim.app.data.mapper.toDomain
import com.aim.app.data.mapper.toEntity
import com.aim.app.domain.model.Task
import com.aim.app.domain.model.TaskOccurrence
import com.aim.app.domain.model.TaskStatus
import com.aim.app.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val database: AimDatabase,
    private val dao: TaskDao,
    private val occurrenceDao: TaskOccurrenceDao,
    private val clock: () -> Instant = Instant::now,
) : TaskRepository {

    override fun observeActiveTasksForGoal(goalId: Long): Flow<List<Task>> =
        dao.observeForGoal(goalId).map { list -> list.map { it.toDomain() } }

    override fun observeTask(id: Long): Flow<Task?> =
        dao.observeById(id).map { it?.toDomain() }

    override fun observeTrashedTasks(): Flow<List<Task>> =
        dao.observeTrashed().map { list -> list.map { it.toDomain() } }

    override fun observeTasksScheduledFor(date: LocalDate): Flow<List<Task>> =
        dao.observeScheduledFor(date.toString()).map { list -> list.map { it.toDomain() } }

    override fun observeRecurringTasks(): Flow<List<Task>> =
        dao.observeRecurringTasks().map { list -> list.map { it.toDomain() } }

    override fun observeOverdueTasks(date: LocalDate): Flow<List<Task>> =
        dao.observeOverdue(date.toString()).map { list -> list.map { it.toDomain() } }

    override fun observeTasksWithDeadline(): Flow<List<Task>> =
        dao.observeWithDeadline().map { list -> list.map { it.toDomain() } }

    override fun observeOccurrencesInRange(
        startInclusive: LocalDate,
        endInclusive: LocalDate,
    ): Flow<List<TaskOccurrence>> =
        occurrenceDao.observeInRange(startInclusive.toString(), endInclusive.toString())
            .map { list -> list.map { it.toDomain() } }

    override fun observeFirstLevelTaskCounts(): Flow<List<com.aim.app.domain.model.GoalTaskTally>> =
        dao.observeFirstLevelCounts().map { list ->
            list.map { com.aim.app.domain.model.GoalTaskTally(it.goalId, it.total, it.done) }
        }

    override fun observeTasksCompletedBetween(
        start: java.time.Instant,
        end: java.time.Instant,
    ): Flow<List<Task>> =
        dao.observeCompletedBetween(start.toEpochMilli(), end.toEpochMilli())
            .map { list -> list.map { it.toDomain() } }

    override suspend fun rescheduleTask(taskId: Long, newDate: LocalDate?) {
        dao.updateScheduledFor(taskId, newDate?.toString())
    }

    override suspend fun isOccurrenceCompleted(taskId: Long, date: LocalDate): Boolean =
        occurrenceDao.getOccurrence(taskId, date.toString())?.status == TaskStatus.COMPLETED.name

    override suspend fun getTask(taskId: Long): Task? =
        dao.getById(taskId)?.toDomain()

    override suspend fun setOccurrenceCompleted(taskId: Long, date: LocalDate, completed: Boolean) {
        if (completed) {
            val existing = occurrenceDao.getOccurrence(taskId, date.toString())
            occurrenceDao.upsert(
                TaskOccurrenceEntity(
                    id = existing?.id ?: 0,
                    taskId = taskId,
                    date = date.toString(),
                    status = TaskStatus.COMPLETED.name,
                    completedAt = clock().toEpochMilli(),
                ),
            )
        } else {
            occurrenceDao.delete(taskId, date.toString())
        }
    }

    override suspend fun createTask(task: Task): Long = database.withTransaction {
        val parentDepth = task.parentTaskId?.let { parentId ->
            requireNotNull(dao.getById(parentId)) { "Parent task $parentId not found" }.depth
        } ?: -1
        val newDepth = parentDepth + 1
        check(newDepth <= Task.MAX_DEPTH) {
            "Maximum task depth ${Task.MAX_DEPTH + 1} exceeded"
        }
        val nextOrder = dao.maxOrderIndex(task.goalId, task.parentTaskId) + 1
        val entity = task
            .copy(depth = newDepth, orderIndex = nextOrder)
            .toEntity()
            .copy(id = 0)
        dao.insert(entity)
    }

    override suspend fun updateTask(task: Task) {
        dao.update(task.toEntity())
    }

    override suspend fun softDelete(taskId: Long) {
        dao.markDeleted(taskId, clock().toEpochMilli())
    }

    override suspend fun restoreFromTrash(taskId: Long) {
        dao.clearDeleted(taskId)
    }

    override suspend fun markCompleted(taskId: Long) {
        dao.markCompleted(taskId, clock().toEpochMilli())
    }

    override suspend fun markInProgress(taskId: Long) {
        dao.markInProgress(taskId)
    }

    override suspend fun moveTask(taskId: Long, newParentId: Long?, newGoalId: Long) {
        database.withTransaction {
            val task = requireNotNull(dao.getById(taskId)) { "Task $taskId not found" }

            val newParentDepth = newParentId?.let { parentId ->
                check(parentId != taskId) { "Task cannot be its own parent" }
                val subtreeIds = dao.getSubtreeIds(taskId).toSet()
                check(parentId !in subtreeIds) {
                    "Cannot move task into its own descendant (cycle)"
                }
                requireNotNull(dao.getById(parentId)) { "Parent task $parentId not found" }.depth
            } ?: -1
            val newDepth = newParentDepth + 1

            val subtreeMaxDepth = computeSubtreeMaxDepth(taskId, task.depth)
            val subtreeHeight = subtreeMaxDepth - task.depth
            check(newDepth + subtreeHeight <= Task.MAX_DEPTH) {
                "Move would exceed max depth ${Task.MAX_DEPTH + 1}"
            }

            val nextOrder = dao.maxOrderIndex(newGoalId, newParentId) + 1
            dao.moveTask(
                id = taskId,
                newParentId = newParentId,
                newGoalId = newGoalId,
                newDepth = newDepth,
                newOrder = nextOrder,
            )
            shiftDescendantDepths(taskId, delta = newDepth - task.depth, newGoalId = newGoalId)
        }
    }

    override suspend fun reorder(parentTaskId: Long?, goalId: Long, orderedIds: List<Long>) {
        dao.reorderInParent(orderedIds)
    }

    override suspend fun permanentlyDelete(taskId: Long) {
        dao.deleteById(taskId)
    }

    override suspend fun getSubtreeIds(taskId: Long): Set<Long> =
        dao.getSubtreeIds(taskId).toSet()

    override suspend fun purgeDeletedBefore(threshold: java.time.Instant): Int =
        dao.purgeDeletedBefore(threshold.toEpochMilli())

    private suspend fun computeSubtreeMaxDepth(rootId: Long, rootDepth: Int): Int {
        val ids = dao.getSubtreeIds(rootId)
        var max = rootDepth
        ids.forEach { id ->
            dao.getById(id)?.let { if (it.depth > max) max = it.depth }
        }
        return max
    }

    private suspend fun shiftDescendantDepths(rootId: Long, delta: Int, newGoalId: Long) {
        if (delta == 0) return
        val ids = dao.getSubtreeIds(rootId).filter { it != rootId }
        ids.forEach { id ->
            val entity = dao.getById(id) ?: return@forEach
            dao.update(entity.copy(depth = entity.depth + delta, goalId = newGoalId))
        }
    }
}
