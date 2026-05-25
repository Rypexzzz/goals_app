package com.aim.app.data.repository

import com.aim.app.data.local.preferences.AppPreferencesDataSource
import com.aim.app.domain.repository.AppPreferencesRepository
import kotlinx.coroutines.flow.Flow
import java.time.DayOfWeek
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppPreferencesRepositoryImpl @Inject constructor(
    private val source: AppPreferencesDataSource,
) : AppPreferencesRepository {

    override fun observeOnboardingCompleted(): Flow<Boolean> = source.onboardingCompleted

    override suspend fun setOnboardingCompleted(completed: Boolean) =
        source.setOnboardingCompleted(completed)

    override fun observeFirstDayOfWeek(): Flow<DayOfWeek> = source.firstDayOfWeek

    override suspend fun setFirstDayOfWeek(day: DayOfWeek) = source.setFirstDayOfWeek(day)
}
