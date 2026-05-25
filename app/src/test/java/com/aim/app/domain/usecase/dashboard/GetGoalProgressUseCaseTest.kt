package com.aim.app.domain.usecase.dashboard

import com.aim.app.domain.model.Goal
import com.aim.app.domain.model.GoalFilter
import com.aim.app.domain.model.GoalStatus
import com.aim.app.domain.model.GoalTaskTally
import com.aim.app.domain.repository.GoalRepository
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

class GetGoalProgressUseCaseTest {

    private val goalRepository: GoalRepository = mockk()
    private val taskRepository: TaskRepository = mockk()
    private val sut = GetGoalProgressUseCase(goalRepository, taskRepository)

    @Test
    fun `joins tallies and sorts by deadline with nulls last`() = runTest {
        val soon = goal(1, "Скоро", LocalDate.of(2026, 6, 1))
        val later = goal(2, "Позже", LocalDate.of(2026, 12, 1))
        val noDeadline = goal(3, "Без дедлайна", null)

        every { goalRepository.observeGoals(GoalFilter.ACTIVE) } returns
            flowOf(listOf(noDeadline, later, soon))
        every { taskRepository.observeFirstLevelTaskCounts() } returns flowOf(
            listOf(
                GoalTaskTally(goalId = 1, total = 4, done = 2),
                GoalTaskTally(goalId = 2, total = 10, done = 10),
            ),
        )

        val result = sut().first()

        assertEquals(listOf(1L, 2L, 3L), result.map { it.goal.id })
        val first = result.first()
        assertEquals(2, first.doneFirstLevel)
        assertEquals(4, first.totalFirstLevel)
        assertEquals(0.5f, first.progress)
        // goal without tally → 0/0
        assertEquals(0, result[2].totalFirstLevel)
    }

    private fun goal(id: Long, title: String, deadline: LocalDate?) = Goal(
        id = id,
        title = title,
        description = null,
        emoji = null,
        deadline = deadline,
        status = GoalStatus.IN_PROGRESS,
        orderIndex = 0,
        createdAt = Instant.EPOCH,
        completedAt = null,
        archivedAt = null,
        deletedAt = null,
    )
}
