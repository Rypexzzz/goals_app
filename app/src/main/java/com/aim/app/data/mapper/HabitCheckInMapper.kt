package com.aim.app.data.mapper

import com.aim.app.data.local.entity.HabitCheckInEntity
import com.aim.app.domain.model.CheckInStatus
import com.aim.app.domain.model.HabitCheckIn
import java.time.Instant
import java.time.LocalDate

fun HabitCheckInEntity.toDomain(): HabitCheckIn = HabitCheckIn(
    id = id,
    habitId = habitId,
    date = LocalDate.parse(date),
    status = runCatching { CheckInStatus.valueOf(status) }.getOrDefault(CheckInStatus.DONE),
    checkedAt = Instant.ofEpochMilli(checkedAt),
)

fun HabitCheckIn.toEntity(): HabitCheckInEntity = HabitCheckInEntity(
    id = id,
    habitId = habitId,
    date = date.toString(),
    status = status.name,
    checkedAt = checkedAt.toEpochMilli(),
)
