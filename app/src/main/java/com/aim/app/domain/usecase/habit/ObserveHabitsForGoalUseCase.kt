package com.aim.app.domain.usecase.habit

import com.aim.app.domain.model.Habit
import com.aim.app.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveHabitsForGoalUseCase @Inject constructor(
    private val repository: HabitRepository,
) {
    operator fun invoke(goalId: Long): Flow<List<Habit>> =
        repository.observeHabitsForGoal(goalId)
}
