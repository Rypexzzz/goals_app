package com.aim.app.domain.usecase.goal

import com.aim.app.domain.model.Goal
import com.aim.app.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveGoalUseCase @Inject constructor(
    private val repository: GoalRepository,
) {
    operator fun invoke(goalId: Long): Flow<Goal?> = repository.observeGoal(goalId)
}
