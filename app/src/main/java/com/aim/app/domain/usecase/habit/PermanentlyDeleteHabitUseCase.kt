package com.aim.app.domain.usecase.habit

import com.aim.app.domain.repository.HabitRepository
import javax.inject.Inject

class PermanentlyDeleteHabitUseCase @Inject constructor(
    private val repository: HabitRepository,
) {
    suspend operator fun invoke(habitId: Long) = repository.permanentlyDelete(habitId)
}
