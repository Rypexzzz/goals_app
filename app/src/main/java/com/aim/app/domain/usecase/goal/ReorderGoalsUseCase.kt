package com.aim.app.domain.usecase.goal

import com.aim.app.domain.repository.GoalRepository
import javax.inject.Inject

class ReorderGoalsUseCase @Inject constructor(
    private val repository: GoalRepository,
) {
    suspend operator fun invoke(orderedIds: List<Long>) = repository.reorder(orderedIds)
}
