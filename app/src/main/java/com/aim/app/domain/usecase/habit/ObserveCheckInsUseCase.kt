package com.aim.app.domain.usecase.habit

import com.aim.app.domain.model.HabitCheckIn
import com.aim.app.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveCheckInsUseCase @Inject constructor(
    private val repository: HabitRepository,
) {
    operator fun invoke(habitId: Long): Flow<List<HabitCheckIn>> =
        repository.observeCheckInsForHabit(habitId)
}
