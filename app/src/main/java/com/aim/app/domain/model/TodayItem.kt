package com.aim.app.domain.model

import java.time.LocalTime

/**
 * Элемент чек-листа «Сегодня». Объединяет разовые задачи, экземпляры регулярных задач
 * и привычки в один сортируемый список.
 */
sealed interface TodayItem {
    val stableKey: String
    val title: String
    val emoji: String?
    val time: LocalTime?
    val isDone: Boolean

    /** Разовая задача (`scheduledFor == today`) либо экземпляр регулярной задачи на сегодня. */
    data class TaskItem(
        val task: Task,
        val isRecurringInstance: Boolean,
        override val isDone: Boolean,
    ) : TodayItem {
        override val stableKey: String =
            if (isRecurringInstance) "task-occ-${task.id}" else "task-${task.id}"
        override val title: String = task.title
        override val emoji: String? = task.emoji
        override val time: LocalTime? = task.scheduledTime
    }

    /** Привычка, актуальная на сегодня. */
    data class HabitItem(
        val habit: Habit,
        val status: CheckInStatus?,
        val currentStreak: Int,
    ) : TodayItem {
        override val stableKey: String = "habit-${habit.id}"
        override val title: String = habit.title
        override val emoji: String? = habit.emoji
        override val time: LocalTime? = null
        override val isDone: Boolean get() = status == CheckInStatus.DONE
        val isFailed: Boolean get() = status == CheckInStatus.FAILED
    }
}

/**
 * Полное состояние данных экрана «Сегодня».
 */
data class TodaySnapshot(
    val todo: List<TodayItem>,
    val doneToday: List<TodayItem>,
    val overdueTasks: List<Task>,
) {
    val totalCount: Int get() = todo.size + doneToday.size
    val doneCount: Int get() = doneToday.size
    val isEmpty: Boolean get() = todo.isEmpty() && doneToday.isEmpty() && overdueTasks.isEmpty()
}
