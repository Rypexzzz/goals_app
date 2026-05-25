package com.aim.app.domain.usecase.today

import com.aim.app.domain.model.Habit
import com.aim.app.domain.model.HabitCheckIn
import com.aim.app.domain.model.Task
import com.aim.app.domain.model.TaskOccurrence
import com.aim.app.domain.model.TaskStatus
import com.aim.app.domain.model.TodayItem
import com.aim.app.domain.model.TodaySnapshot
import com.aim.app.domain.repository.HabitRepository
import com.aim.app.domain.repository.TaskRepository
import com.aim.app.domain.usecase.habit.CalculateStreakUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject

/**
 * Собирает чек-лист «Сегодня»: разовые задачи на сегодня, экземпляры регулярных задач,
 * привычки, актуальные на сегодня (README §6.4), плюс просроченные задачи.
 *
 * Сортировка: сначала задачи, затем привычки; внутри — по времени, затем по orderIndex.
 */
class GetTodayItemsUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
    private val habitRepository: HabitRepository,
    private val calculateStreak: CalculateStreakUseCase,
) {
    operator fun invoke(today: LocalDate = LocalDate.now()): Flow<TodaySnapshot> {
        val tasksFlow = combine(
            taskRepository.observeTasksScheduledFor(today),
            taskRepository.observeRecurringTasks(),
            taskRepository.observeOccurrencesInRange(today, today),
            taskRepository.observeOverdueTasks(today),
        ) { scheduled, recurring, occurrences, overdue ->
            TaskBundle(scheduled, recurring, occurrences, overdue)
        }

        val habitsFlow = combine(
            habitRepository.observeActiveHabits(),
            habitRepository.observeAllCheckIns(),
        ) { habits, checkIns ->
            HabitBundle(habits, checkIns)
        }

        return combine(tasksFlow, habitsFlow) { tasks, habits ->
            buildSnapshot(today, tasks, habits)
        }
    }

    private fun buildSnapshot(
        today: LocalDate,
        tasks: TaskBundle,
        habits: HabitBundle,
    ): TodaySnapshot {
        val items = mutableListOf<TodayItem>()

        // 1) Разовые задачи на сегодня.
        tasks.scheduled.forEach { task ->
            items += TodayItem.TaskItem(
                task = task,
                isRecurringInstance = false,
                isDone = task.status == TaskStatus.COMPLETED,
            )
        }

        // 2) Экземпляры регулярных задач, выпадающие на сегодня.
        val completedOccurrenceTaskIds = tasks.occurrences
            .filter { it.date == today && it.status == TaskStatus.COMPLETED }
            .map { it.taskId }
            .toSet()
        tasks.recurring.forEach { task ->
            val recurrence = task.recurrence ?: return@forEach
            val anchor = task.scheduledFor ?: task.createdAt.atZone(ZoneId.systemDefault()).toLocalDate()
            if (RecurrenceMatcher.occursOn(recurrence, today, anchor)) {
                items += TodayItem.TaskItem(
                    task = task,
                    isRecurringInstance = true,
                    isDone = task.id in completedOccurrenceTaskIds,
                )
            }
        }

        // 3) Привычки, актуальные на сегодня.
        val checkInsByHabit: Map<Long, List<HabitCheckIn>> = habits.checkIns.groupBy { it.habitId }
        habits.habits.forEach { habit ->
            val habitCheckIns = checkInsByHabit[habit.id].orEmpty()
            val hasTodayEntry = habitCheckIns.any { it.date == today }
            if (HabitScheduler.isDueOn(habit.frequency, today, habitCheckIns) || hasTodayEntry) {
                val todayStatus = habitCheckIns.firstOrNull { it.date == today }?.status
                items += TodayItem.HabitItem(
                    habit = habit,
                    status = todayStatus,
                    currentStreak = calculateStreak(habit.frequency, habitCheckIns, today).current,
                )
            }
        }

        val sorted = items.sortedWith(todayComparator)
        val (done, todo) = sorted.partition { it.isDone }

        return TodaySnapshot(
            todo = todo,
            doneToday = done,
            overdueTasks = tasks.overdue,
        )
    }

    private val todayComparator: Comparator<TodayItem> = compareBy(
        { it !is TodayItem.TaskItem }, // задачи раньше привычек
        { (it.time ?: LocalTime.MAX) },
        {
            when (it) {
                is TodayItem.TaskItem -> it.task.orderIndex
                is TodayItem.HabitItem -> it.habit.orderIndex
            }
        },
    )

    private data class TaskBundle(
        val scheduled: List<Task>,
        val recurring: List<Task>,
        val occurrences: List<TaskOccurrence>,
        val overdue: List<Task>,
    )

    private data class HabitBundle(
        val habits: List<Habit>,
        val checkIns: List<HabitCheckIn>,
    )
}
