package com.aim.app.domain.usecase.goal

import com.aim.app.domain.repository.GoalRepository
import javax.inject.Inject

class RestoreGoalUseCase @Inject constructor(
    private val repository: GoalRepository,
) {
    suspend operator fun invoke(goalId: Long) = repository.restoreFromTrash(goalId)
}
