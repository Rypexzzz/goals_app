package com.aim.app.domain.usecase.today

import com.aim.app.domain.model.CheckInStatus
import com.aim.app.domain.repository.HabitRepository
import com.aim.app.domain.repository.TaskRepository
import com.aim.app.domain.usecase.habit.CheckInHabitUseCase
import com.aim.app.domain.usecase.habit.UncheckHabitUseCase
import com.aim.app.domain.usecase.task.CompleteTaskUseCase
import com.aim.app.domain.usecase.task.SetTaskOccurrenceUseCase
import com.aim.app.domain.usecase.task.UncompleteTaskUseCase
import java.time.LocalDate
import javax.inject.Inject

/**
 * Переключает выполнение элемента «Сегодня» по его [TodayItem.stableKey].
 * Используется виджетом (ActionCallback не имеет прямого доступа к ViewModel).
 *
 * Формат ключа: `task-{id}` | `task-occ-{id}` | `habit-{id}`.
 */
class ToggleTodayItemUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
    private val habitRepository: HabitRepository,
    private val completeTask: CompleteTaskUseCase,
    private val uncompleteTask: UncompleteTaskUseCase,
    private val setTaskOccurrence: SetTaskOccurrenceUseCase,
    private val checkInHabit: CheckInHabitUseCase,
    private val uncheckHabit: UncheckHabitUseCase,
) {
    suspend operator fun invoke(stableKey: String, today: LocalDate = LocalDate.now()) {
        when {
            stableKey.startsWith(PREFIX_OCCURRENCE) -> {
                val id = stableKey.removePrefix(PREFIX_OCCURRENCE).toLongOrNull() ?: return
                val done = taskRepository.isOccurrenceCompleted(id, today)
                setTaskOccurrence(id, today, completed = !done)
            }
            stableKey.startsWith(PREFIX_TASK) -> {
                val id = stableKey.removePrefix(PREFIX_TASK).toLongOrNull() ?: return
                val task = taskRepository.getTask(id) ?: return
                if (task.isCompleted) uncompleteTask(id) else completeTask(id)
            }
            stableKey.startsWith(PREFIX_HABIT) -> {
                val id = stableKey.removePrefix(PREFIX_HABIT).toLongOrNull() ?: return
                if (habitRepository.getCheckInStatus(id, today) == CheckInStatus.DONE) {
                    uncheckHabit(id, today)
                } else {
                    checkInHabit(id, today, CheckInStatus.DONE)
                }
            }
        }
    }

    private companion object {
        const val PREFIX_OCCURRENCE = "task-occ-"
        const val PREFIX_TASK = "task-"
        const val PREFIX_HABIT = "habit-"
    }
}
