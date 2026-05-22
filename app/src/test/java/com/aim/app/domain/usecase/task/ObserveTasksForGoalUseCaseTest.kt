package com.aim.app.domain.usecase.task

import com.aim.app.domain.model.Recurrence
import com.aim.app.domain.model.Task
import com.aim.app.domain.model.TaskStatus
import com.aim.app.domain.repository.TaskRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant

class ObserveTasksForGoalUseCaseTest {

    private val repository: TaskRepository = mockk()
    private val sut = ObserveTasksForGoalUseCase(repository)

    @Test
    fun `builds correctly ordered forest from flat task list`() = runTest {
        every { repository.observeActiveTasksForGoal(1) } returns flowOf(
            // intentionally jumbled order to verify sorting
            task(id = 11, parentId = 1, depth = 1, order = 1),
            task(id = 2, parentId = null, depth = 0, order = 1),
            task(id = 10, parentId = 1, depth = 1, order = 0),
            task(id = 1, parentId = null, depth = 0, order = 0),
            task(id = 100, parentId = 10, depth = 2, order = 0),
        )

        val roots = sut(1).first()

        assertEquals(2, roots.size)
        assertEquals(listOf(1L, 2L), roots.map { it.task.id })
        val firstRoot = roots[0]
        assertEquals(2, firstRoot.children.size)
        assertEquals(listOf(10L, 11L), firstRoot.children.map { it.task.id })
        // Deepest
        assertEquals(listOf(100L), firstRoot.children[0].children.map { it.task.id })
        assertEquals(emptyList<Long>(), firstRoot.children[1].children.map { it.task.id })
    }

    @Test
    fun `orphans whose parent is missing are dropped`() = runTest {
        every { repository.observeActiveTasksForGoal(1) } returns flowOf(
            task(id = 1, parentId = null, depth = 0, order = 0),
            task(id = 99, parentId = 555, depth = 1, order = 0), // parent 555 doesn't exist
        )

        val roots = sut(1).first()

        // The orphan with non-existent parent is not attached anywhere.
        assertEquals(1, roots.size)
        assertEquals(1L, roots[0].task.id)
        assertEquals(0, roots[0].children.size)
    }

    private fun task(id: Long, parentId: Long?, depth: Int, order: Int): Task = Task(
        id = id,
        goalId = 1,
        parentTaskId = parentId,
        title = "T$id",
        description = null,
        emoji = null,
        deadline = null,
        scheduledFor = null,
        scheduledTime = null,
        status = TaskStatus.IN_PROGRESS,
        depth = depth,
        orderIndex = order,
        recurrence = null as Recurrence?,
        createdAt = Instant.EPOCH,
        completedAt = null,
        deletedAt = null,
    )

}
