package com.aim.app.domain.usecase.task

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
import java.time.LocalDate

class GetDeadlineTasksUseCaseTest {

    private val taskRepository: TaskRepository = mockk()
    private val sut = GetDeadlineTasksUseCase(taskRepository)

    @Test
    fun `excludes completed and sorts by deadline ascending`() = runTest {
        every { taskRepository.observeTasksWithDeadline() } returns flowOf(
            listOf(
                task(id = 1, deadline = LocalDate.parse("2026-06-10"), status = TaskStatus.IN_PROGRESS),
                task(id = 2, deadline = LocalDate.parse("2026-05-01"), status = TaskStatus.IN_PROGRESS),
                task(id = 3, deadline = LocalDate.parse("2026-05-20"), status = TaskStatus.COMPLETED),
            ),
        )

        val result = sut().first()

        // id=3 выполнена → исключена; остальные отсортированы по дедлайну (01.05 раньше 10.06).
        assertEquals(listOf(2L, 1L), result.map { it.id })
    }

    private fun task(id: Long, deadline: LocalDate, status: TaskStatus) = Task(
        id = id,
        goalId = 1,
        parentTaskId = null,
        title = "T$id",
        description = null,
        emoji = null,
        deadline = deadline,
        scheduledFor = null,
        scheduledTime = null,
        status = status,
        depth = 0,
        orderIndex = 0,
        recurrence = null,
        createdAt = Instant.EPOCH,
        completedAt = null,
        deletedAt = null,
    )
}
