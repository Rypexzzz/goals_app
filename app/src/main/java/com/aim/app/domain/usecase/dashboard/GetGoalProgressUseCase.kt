package com.aim.app.domain.usecase.dashboard

import com.aim.app.domain.model.GoalFilter
import com.aim.app.domain.model.GoalProgress
import com.aim.app.domain.repository.GoalRepository
import com.aim.app.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import javax.inject.Inject

class GetGoalProgressUseCase @Inject constructor(
    private val goalRepository: GoalRepository,
    private val taskRepository: TaskRepository,
) {
    operator fun invoke(): Flow<List<GoalProgress>> =
        combine(
            goalRepository.observeGoals(GoalFilter.ACTIVE),
            taskRepository.observeFirstLevelTaskCounts(),
        ) { goals, tallies ->
            val byGoal = tallies.associateBy { it.goalId }
            goals
                .map { goal ->
                    val tally = byGoal[goal.id]
                    GoalProgress(
                        goal = goal,
                        doneFirstLevel = tally?.done ?: 0,
                        totalFirstLevel = tally?.total ?: 0,
                    )
                }
                .sortedWith(compareBy(nullsLast<LocalDate>()) { it.goal.deadline })
        }
}
