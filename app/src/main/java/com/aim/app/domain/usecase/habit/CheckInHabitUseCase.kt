package com.aim.app.domain.usecase.habit

import com.aim.app.domain.model.CheckInStatus
import com.aim.app.domain.repository.HabitRepository
import java.time.LocalDate
import javax.inject.Inject

class CheckInHabitUseCase @Inject constructor(
    private val repository: HabitRepository,
) {
    suspend operator fun invoke(habitId: Long, date: LocalDate, status: CheckInStatus) {
        repository.upsertCheckIn(habitId, date, status)
    }
}
