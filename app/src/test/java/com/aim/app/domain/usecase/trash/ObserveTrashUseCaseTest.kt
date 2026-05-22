package com.aim.app.domain.usecase.trash

import com.aim.app.domain.model.Goal
import com.aim.app.domain.model.GoalStatus
import com.aim.app.domain.model.Task
import com.aim.app.domain.model.TaskStatus
import com.aim.app.domain.model.TrashItem
import com.aim.app.domain.repository.GoalRepository
import com.aim.app.domain.repository.TaskRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant

class ObserveTrashUseCaseTest {

    private val goals: GoalRepository = mockk()
    private val tasks: TaskRepository = mockk()
    private val sut = ObserveTrashUseCase(goals, tasks)

    @Test
    fun `combines deleted goals and tasks sorted by deletedAt desc`() = runTest {
        val t1 = Instant.parse("2026-05-20T10:00:00Z")
        val t2 = Instant.parse("2026-05-21T10:00:00Z")
        val t3 = Instant.parse("2026-05-22T10:00:00Z")

        val goalAlive = goal(id = 10, title = "Цель А", deletedAt = null)
        val goalTrashed = goal(id = 20, title = "Удалённая цель", deletedAt = t2)
        val taskInAlive = task(id = 1, goalId = 10, deletedAt = t1)
        val taskInTrashed = task(id = 2, goalId = 20, deletedAt = t3)

        every { goals.observeAllIncludingDeleted() } returns flowOf(listOf(goalAlive, goalTrashed))
        every { tasks.observeTrashedTasks() } returns flowOf(listOf(taskInAlive, taskInTrashed))

        val result = sut().first()

        // 3 items: goalTrashed + 2 tasks (both their goals appear in the list goals, so both are surfaced)
        assertEquals(3, result.size)
        // Sorted by deletedAt desc: t3 (taskInTrashed), t2 (goalTrashed), t1 (taskInAlive)
        assertEquals(t3, result[0].deletedAt)
        assertEquals(t2, result[1].deletedAt)
        assertEquals(t1, result[2].deletedAt)
        assertTrue(result.any { it is TrashItem.GoalItem && it.goal.id == 20L })
        assertTrue(result.any { it is TrashItem.TaskItem && it.task.id == 1L && it.goalTitle == "Цель А" })
    }

    private fun goal(id: Long, title: String, deletedAt: Instant?): Goal = Goal(
        id = id,
        title = title,
        description = null,
        emoji = null,
        deadline = null,
        status = GoalStatus.IN_PROGRESS,
        orderIndex = 0,
        createdAt = Instant.EPOCH,
        completedAt = null,
        archivedAt = null,
        deletedAt = deletedAt,
    )

    private fun task(id: Long, goalId: Long, deletedAt: Instant): Task = Task(
        id = id,
        goalId = goalId,
        parentTaskId = null,
        title = "T$id",
        description = null,
        emoji = null,
        deadline = null,
        scheduledFor = null,
        scheduledTime = null,
        status = TaskStatus.IN_PROGRESS,
        depth = 0,
        orderIndex = 0,
        recurrence = null,
        createdAt = Instant.EPOCH,
        completedAt = null,
        deletedAt = deletedAt,
    )

}
