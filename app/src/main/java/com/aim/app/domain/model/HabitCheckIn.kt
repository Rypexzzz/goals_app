package com.aim.app.domain.model

import java.time.Instant
import java.time.LocalDate

data class HabitCheckIn(
    val id: Long,
    val habitId: Long,
    val date: LocalDate,
    val status: CheckInStatus,
    val checkedAt: Instant,
)
