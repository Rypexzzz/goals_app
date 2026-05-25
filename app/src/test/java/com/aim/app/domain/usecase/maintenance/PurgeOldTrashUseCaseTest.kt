package com.aim.app.domain.usecase.maintenance

import com.aim.app.domain.repository.GoalRepository
import com.aim.app.domain.repository.HabitRepository
import com.aim.app.domain.repository.TaskRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

class PurgeOldTrashUseCaseTest {

    private val goalRepository: GoalRepository = mockk()
    private val taskRepository: TaskRepository = mockk()
    private val habitRepository: HabitRepository = mockk()
    private val fixedNow = Instant.parse("2026-05-23T12:00:00Z")
    private val sut = PurgeOldTrashUseCase(goalRepository, taskRepository, habitRepository) { fixedNow }

    @Test
    fun `passes threshold of now minus retention days and sums results`() = runTest {
        val goalThreshold = slot<Instant>()
        coEvery { goalRepository.purgeDeletedBefore(capture(goalThreshold)) } returns 2
        coEvery { taskRepository.purgeDeletedBefore(any()) } returns 3
        coEvery { habitRepository.purgeDeletedBefore(any()) } returns 1

        val purged = sut(retentionDays = 30)

        assertEquals(6, purged)
        val expectedThreshold = fixedNow.minus(30, ChronoUnit.DAYS)
        assertEquals(expectedThreshold, goalThreshold.captured)
    }

    @Test
    fun `uses default 30-day retention`() = runTest {
        val captured = slot<Instant>()
        coEvery { goalRepository.purgeDeletedBefore(capture(captured)) } returns 0
        coEvery { taskRepository.purgeDeletedBefore(any()) } returns 0
        coEvery { habitRepository.purgeDeletedBefore(any()) } returns 0

        sut()

        assertTrue(captured.captured == fixedNow.minus(30, ChronoUnit.DAYS))
        coVerify(exactly = 1) { taskRepository.purgeDeletedBefore(any()) }
    }
}
