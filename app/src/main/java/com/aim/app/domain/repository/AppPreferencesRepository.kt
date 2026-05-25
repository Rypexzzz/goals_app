package com.aim.app.domain.repository

import kotlinx.coroutines.flow.Flow
import java.time.DayOfWeek

interface AppPreferencesRepository {

    fun observeOnboardingCompleted(): Flow<Boolean>

    suspend fun setOnboardingCompleted(completed: Boolean)

    fun observeFirstDayOfWeek(): Flow<DayOfWeek>

    suspend fun setFirstDayOfWeek(day: DayOfWeek)
}
