package com.aim.app.domain.repository

import com.aim.app.domain.model.Task
import kotlinx.coroutines.flow.Flow

interface TaskRepository {

    /** Все активные (не удалённые) задачи для цели, плоским списком. Сортировка не гарантируется. */
    fun observeActiveTasksForGoal(goalId: Long): Flow<List<Task>>

    fun observeTask(id: Long): Flow<Task?>

    /** Все удалённые задачи (для экрана корзины). */
    fun observeTrashedTasks(): Flow<List<Task>>

    /**
     * Создание задачи. Реализация устанавливает `depth` и `orderIndex` автоматически из parentTaskId.
     * Если parentTaskId.depth == [Task.MAX_DEPTH] — бросает [IllegalStateException].
     */
    suspend fun createTask(task: Task): Long

    suspend fun updateTask(task: Task)

    suspend fun softDelete(taskId: Long)

    suspend fun restoreFromTrash(taskId: Long)

    suspend fun markCompleted(taskId: Long)

    suspend fun markInProgress(taskId: Long)

    /**
     * Изменить родителя задачи. Должен проверять отсутствие циклов и ограничение глубины
     * (новый parent.depth + 1 + поддерево не превышает [Task.MAX_DEPTH]).
     */
    suspend fun moveTask(taskId: Long, newParentId: Long?, newGoalId: Long)

    /** Переупорядочить задачи в пределах одного родителя. */
    suspend fun reorder(parentTaskId: Long?, goalId: Long, orderedIds: List<Long>)

    suspend fun permanentlyDelete(taskId: Long)

    /** Получить идентификаторы всех потомков задачи (включая саму) — для проверки циклов. */
    suspend fun getSubtreeIds(taskId: Long): Set<Long>
}
