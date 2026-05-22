package com.aim.app.domain.usecase.habit

import com.aim.app.domain.repository.HabitRepository
import java.time.LocalDate
import javax.inject.Inject

class UncheckHabitUseCase @Inject constructor(
    private val repository: HabitRepository,
) {
    suspend operator fun invoke(habitId: Long, date: LocalDate) {
        repository.deleteCheckIn(habitId, date)
    }
}
