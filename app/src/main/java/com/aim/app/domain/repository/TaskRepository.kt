package com.aim.app.domain.repository

import com.aim.app.domain.model.GoalTaskTally
import com.aim.app.domain.model.Task
import com.aim.app.domain.model.TaskOccurrence
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.LocalDate

interface TaskRepository {

    /** Все активные (не удалённые) задачи для цели, плоским списком. Сортировка не гарантируется. */
    fun observeActiveTasksForGoal(goalId: Long): Flow<List<Task>>

    fun observeTask(id: Long): Flow<Task?>

    /** Все удалённые задачи (для экрана корзины). */
    fun observeTrashedTasks(): Flow<List<Task>>

    /** Разовые задачи, запланированные на дату (для экрана «Сегодня»). */
    fun observeTasksScheduledFor(date: LocalDate): Flow<List<Task>>

    /** Все живые регулярные задачи (recurrence != null). */
    fun observeRecurringTasks(): Flow<List<Task>>

    /** Просроченные разовые задачи (scheduledFor < date, не выполнены). */
    fun observeOverdueTasks(date: LocalDate): Flow<List<Task>>

    /** Все живые задачи с дедлайном (из активных целей), отсортированные по дате дедлайна. */
    fun observeTasksWithDeadline(): Flow<List<Task>>

    /** Экземпляры регулярных задач в диапазоне дат. */
    fun observeOccurrencesInRange(
        startInclusive: LocalDate,
        endInclusive: LocalDate,
    ): Flow<List<TaskOccurrence>>

    /** Счётчики задач первого уровня по целям — для прогресса на дашборде. */
    fun observeFirstLevelTaskCounts(): Flow<List<GoalTaskTally>>

    /** Разовые задачи, выполненные в диапазоне — для статистики периода. */
    fun observeTasksCompletedBetween(start: Instant, end: Instant): Flow<List<Task>>

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

    /** Перенести разовую задачу на новую дату (snooze / изменить дату / убрать из расписания). */
    suspend fun rescheduleTask(taskId: Long, newDate: LocalDate?)

    /** Отметить/снять выполнение экземпляра регулярной задачи на дату. */
    suspend fun setOccurrenceCompleted(taskId: Long, date: LocalDate, completed: Boolean)

    /** Выполнен ли экземпляр регулярной задачи на дату. */
    suspend fun isOccurrenceCompleted(taskId: Long, date: LocalDate): Boolean

    /** Снимок задачи по id (одноразовое чтение). */
    suspend fun getTask(taskId: Long): Task?

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

    /** Окончательно удалить задачи в корзине, удалённые раньше [threshold]. */
    suspend fun purgeDeletedBefore(threshold: java.time.Instant): Int
}
