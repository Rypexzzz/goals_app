package com.aim.app.domain.usecase.task

import com.aim.app.domain.model.Recurrence
import com.aim.app.domain.model.Task
import com.aim.app.domain.repository.TaskRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate

class CreateTaskUseCaseTest {

    private val repository: TaskRepository = mockk(relaxed = true)
    private val fixedNow = Instant.parse("2026-05-22T10:00:00Z")
    private val sut = CreateTaskUseCase(repository, clock = { fixedNow })

    @Test
    fun `blank title is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            kotlinx.coroutines.test.runTest {
                sut(goalId = 1, parentTaskId = null, title = "   ")
            }
        }
    }

    @Test
    fun `forwards trimmed title and default status to repository`() = runTest {
        val captured = slot<Task>()
        coEvery { repository.createTask(capture(captured)) } returns 42L

        val resultId = sut(
            goalId = 7,
            parentTaskId = null,
            title = "  Купить экипировку  ",
            description = "С новыми кроссовками",
            emoji = "🏋️",
        )

        assertEquals(42L, resultId)
        coVerify(exactly = 1) { repository.createTask(any()) }
        val task = captured.captured
        assertEquals("Купить экипировку", task.title)
        assertEquals("С новыми кроссовками", task.description)
        assertEquals("🏋️", task.emoji)
        assertEquals(7L, task.goalId)
        assertNull(task.parentTaskId)
        assertEquals(fixedNow, task.createdAt)
    }

    @Test
    fun `blank description and emoji become null`() = runTest {
        val captured = slot<Task>()
        coEvery { repository.createTask(capture(captured)) } returns 1L

        sut(goalId = 1, parentTaskId = null, title = "T", description = "  ", emoji = "")
        val task = captured.captured
        assertNull(task.description)
        assertNull(task.emoji)
    }

    @Test
    fun `recurrence is passed through to repository`() = runTest {
        val captured = slot<Task>()
        coEvery { repository.createTask(capture(captured)) } returns 1L

        sut(
            goalId = 1,
            parentTaskId = null,
            title = "Каждый день",
            recurrence = Recurrence.Daily,
            scheduledFor = LocalDate.of(2026, 6, 1),
        )

        assertEquals(Recurrence.Daily, captured.captured.recurrence)
        assertNotNull(captured.captured.scheduledFor)
    }
}
