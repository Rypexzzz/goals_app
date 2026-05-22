package com.aim.app.domain.usecase.goal

import com.aim.app.domain.model.Goal
import com.aim.app.domain.repository.GoalRepository
import javax.inject.Inject

class UpdateGoalUseCase @Inject constructor(
    private val repository: GoalRepository,
) {
    suspend operator fun invoke(goal: Goal) {
        val trimmed = goal.title.trim()
        require(trimmed.isNotEmpty()) { "Goal title must not be blank" }
        repository.updateGoal(
            goal.copy(
                title = trimmed,
                description = goal.description?.takeIf { it.isNotBlank() },
                emoji = goal.emoji?.takeIf { it.isNotBlank() },
            ),
        )
    }
}
