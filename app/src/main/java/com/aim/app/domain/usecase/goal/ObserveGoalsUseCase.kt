package com.aim.app.domain.usecase.goal

import com.aim.app.domain.model.Goal
import com.aim.app.domain.model.GoalFilter
import com.aim.app.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveGoalsUseCase @Inject constructor(
    private val repository: GoalRepository,
) {
    operator fun invoke(filter: GoalFilter): Flow<List<Goal>> = repository.observeGoals(filter)
}
