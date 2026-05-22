package com.aim.app.domain.usecase.goal

import com.aim.app.domain.model.Goal
import com.aim.app.domain.model.GoalStatus
import com.aim.app.domain.repository.GoalRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate

class CreateGoalUseCaseTest {

    private val repository: GoalRepository = mockk(relaxed = true)
    private val fixedNow = Instant.parse("2026-05-22T10:00:00Z")
    private val sut = CreateGoalUseCase(repository, clock = { fixedNow })

    @Test
    fun `blank title is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            runTest { sut(title = "  ", description = null, emoji = null, deadline = null) }
        }
    }

    @Test
    fun `trims fields and sets defaults`() = runTest {
        val captured = slot<Goal>()
        coEvery { repository.createGoal(capture(captured)) } returns 99L

        val id = sut(
            title = "  Стать сильнее  ",
            description = "  Тренировки  ",
            emoji = "💪",
            deadline = LocalDate.of(2026, 12, 31),
        )

        assertEquals(99L, id)
        coVerify(exactly = 1) { repository.createGoal(any()) }
        val goal = captured.captured
        assertEquals("Стать сильнее", goal.title)
        assertEquals("Тренировки", goal.description)
        assertEquals("💪", goal.emoji)
        assertEquals(GoalStatus.IN_PROGRESS, goal.status)
        assertEquals(fixedNow, goal.createdAt)
    }

    @Test
    fun `blank description becomes null`() = runTest {
        val captured = slot<Goal>()
        coEvery { repository.createGoal(capture(captured)) } returns 1L

        sut(title = "T", description = "", emoji = null, deadline = null)
        assertNull(captured.captured.description)
    }
}
