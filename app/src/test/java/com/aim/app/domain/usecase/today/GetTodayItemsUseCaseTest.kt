package com.aim.app.domain.usecase.today

import com.aim.app.domain.model.CheckInStatus
import com.aim.app.domain.model.Goal
import com.aim.app.domain.model.GoalStatus
import com.aim.app.domain.model.Habit
import com.aim.app.domain.model.HabitCheckIn
import com.aim.app.domain.model.HabitFrequency
import com.aim.app.domain.model.Recurrence
import com.aim.app.domain.model.Task
import com.aim.app.domain.model.TaskOccurrence
import com.aim.app.domain.model.TaskStatus
import com.aim.app.domain.model.TodayItem
import com.aim.app.domain.repository.HabitRepository
import com.aim.app.domain.repository.TaskRepository
import com.aim.app.domain.usecase.habit.CalculateStreakUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

class GetTodayItemsUseCaseTest {

    private val taskRepository: TaskRepository = mockk()
    private val habitRepository: HabitRepository = mockk()
    private val sut = GetTodayItemsUseCase(taskRepository, habitRepository, CalculateStreakUseCase())

    private val today = LocalDate.of(2026, 5, 22)

    @Test
    fun `combines scheduled task, recurring instance and daily habit; sorts tasks before habits`() = runTest {
        val scheduledTask = task(id = 1, title = "Позвонить врачу", time = LocalTime.of(9, 0))
        val recurringTask = task(
            id = 2, title = "Зарядка", recurrence = Recurrence.Daily,
            scheduledFor = today.minusDays(5),
        )
        val habit = habit(id = 3, title = "Не курить", frequency = HabitFrequency.Daily)

        every { taskRepository.observeTasksScheduledFor(today) } returns flowOf(listOf(scheduledTask))
        every { taskRepository.observeRecurringTasks() } returns flowOf(listOf(recurringTask))
        every { taskRepository.observeOccurrencesInRange(today, today) } returns flowOf(emptyList())
        every { taskRepository.observeOverdueTasks(today) } returns flowOf(emptyList())
        every { habitRepository.observeActiveHabits() } returns flowOf(listOf(habit))
        every { habitRepository.observeAllCheckIns() } returns flowOf(emptyList())

        val snapshot = sut(today).first()

        // 2 tasks + 1 habit, none done
        assertEquals(3, snapshot.todo.size)
        assertEquals(0, snapshot.doneToday.size)
        // tasks come first
        assertTrue(snapshot.todo[0] is TodayItem.TaskItem)
        assertTrue(snapshot.todo[1] is TodayItem.TaskItem)
        assertTrue(snapshot.todo[2] is TodayItem.HabitItem)
    }

    @Test
    fun `completed scheduled task goes to done section`() = runTest {
        val doneTask = task(id = 1, title = "Готово", status = TaskStatus.COMPLETED)
        every { taskRepository.observeTasksScheduledFor(today) } returns flowOf(listOf(doneTask))
        every { taskRepository.observeRecurringTasks() } returns flowOf(emptyList())
        every { taskRepository.observeOccurrencesInRange(today, today) } returns flowOf(emptyList())
        every { taskRepository.observeOverdueTasks(today) } returns flowOf(emptyList())
        every { habitRepository.observeActiveHabits() } returns flowOf(emptyList())
        every { habitRepository.observeAllCheckIns() } returns flowOf(emptyList())

        val snapshot = sut(today).first()
        assertEquals(0, snapshot.todo.size)
        assertEquals(1, snapshot.doneToday.size)
    }

    @Test
    fun `recurring instance marked done via occurrence lands in done section`() = runTest {
        val recurringTask = task(
            id = 2, title = "Зарядка", recurrence = Recurrence.Daily, scheduledFor = today.minusDays(3),
        )
        val occurrence = TaskOccurrence(
            id = 1, taskId = 2, date = today, status = TaskStatus.COMPLETED, completedAt = Instant.EPOCH,
        )
        every { taskRepository.observeTasksScheduledFor(today) } returns flowOf(emptyList())
        every { taskRepository.observeRecurringTasks() } returns flowOf(listOf(recurringTask))
        every { taskRepository.observeOccurrencesInRange(today, today) } returns flowOf(listOf(occurrence))
        every { taskRepository.observeOverdueTasks(today) } returns flowOf(emptyList())
        every { habitRepository.observeActiveHabits() } returns flowOf(emptyList())
        every { habitRepository.observeAllCheckIns() } returns flowOf(emptyList())

        val snapshot = sut(today).first()
        assertEquals(0, snapshot.todo.size)
        assertEquals(1, snapshot.doneToday.size)
        val item = snapshot.doneToday.first() as TodayItem.TaskItem
        assertTrue(item.isRecurringInstance)
        assertTrue(item.isDone)
    }

    @Test
    fun `done habit goes to done section, failed habit stays in todo`() = runTest {
        val doneHabit = habit(id = 1, title = "Done", frequency = HabitFrequency.Daily)
        val failedHabit = habit(id = 2, title = "Failed", frequency = HabitFrequency.Daily)
        every { taskRepository.observeTasksScheduledFor(today) } returns flowOf(emptyList())
        every { taskRepository.observeRecurringTasks() } returns flowOf(emptyList())
        every { taskRepository.observeOccurrencesInRange(today, today) } returns flowOf(emptyList())
        every { taskRepository.observeOverdueTasks(today) } returns flowOf(emptyList())
        every { habitRepository.observeActiveHabits() } returns flowOf(listOf(doneHabit, failedHabit))
        every { habitRepository.observeAllCheckIns() } returns flowOf(
            listOf(
                HabitCheckIn(0, 1, today, CheckInStatus.DONE, Instant.EPOCH),
                HabitCheckIn(0, 2, today, CheckInStatus.FAILED, Instant.EPOCH),
            ),
        )

        val snapshot = sut(today).first()
        assertEquals(1, snapshot.doneToday.size)
        assertEquals(1, snapshot.todo.size)
        assertTrue((snapshot.todo.first() as TodayItem.HabitItem).isFailed)
    }

    @Test
    fun `overdue tasks surface separately`() = runTest {
        val overdue = task(id = 9, title = "Просрочено", scheduledFor = today.minusDays(2))
        every { taskRepository.observeTasksScheduledFor(today) } returns flowOf(emptyList())
        every { taskRepository.observeRecurringTasks() } returns flowOf(emptyList())
        every { taskRepository.observeOccurrencesInRange(today, today) } returns flowOf(emptyList())
        every { taskRepository.observeOverdueTasks(today) } returns flowOf(listOf(overdue))
        every { habitRepository.observeActiveHabits() } returns flowOf(emptyList())
        every { habitRepository.observeAllCheckIns() } returns flowOf(emptyList())

        val snapshot = sut(today).first()
        assertEquals(1, snapshot.overdueTasks.size)
        assertEquals(9L, snapshot.overdueTasks.first().id)
    }

    // -- builders -------------------------------------------------------------

    private fun task(
        id: Long,
        title: String,
        status: TaskStatus = TaskStatus.IN_PROGRESS,
        recurrence: Recurrence? = null,
        scheduledFor: LocalDate? = today,
        time: LocalTime? = null,
    ) = Task(
        id = id, goalId = 1, parentTaskId = null, title = title, description = null, emoji = null,
        deadline = null, scheduledFor = scheduledFor, scheduledTime = time, status = status,
        depth = 0, orderIndex = 0, recurrence = recurrence,
        createdAt = Instant.EPOCH, completedAt = null, deletedAt = null,
    )

    private fun habit(id: Long, title: String, frequency: HabitFrequency) = Habit(
        id = id, goalId = null, title = title, description = null, emoji = null,
        frequency = frequency, orderIndex = 0, createdAt = Instant.EPOCH,
        archivedAt = null, deletedAt = null,
    )

    @Suppress("unused")
    private fun goal() = Goal(
        id = 1, title = "G", description = null, emoji = null, deadline = null,
        status = GoalStatus.IN_PROGRESS, orderIndex = 0, createdAt = Instant.EPOCH,
        completedAt = null, archivedAt = null, deletedAt = null,
    )
}
