package com.aim.app.data.local.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppPreferencesDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    private object Keys {
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_completed")
        val FIRST_DAY_OF_WEEK = intPreferencesKey("first_day_of_week")
    }

    val onboardingCompleted: Flow<Boolean> = dataStore.data.map { it[Keys.ONBOARDING_DONE] ?: false }

    val firstDayOfWeek: Flow<DayOfWeek> = dataStore.data.map { prefs ->
        val value = prefs[Keys.FIRST_DAY_OF_WEEK] ?: DayOfWeek.MONDAY.value
        runCatching { DayOfWeek.of(value) }.getOrDefault(DayOfWeek.MONDAY)
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { it[Keys.ONBOARDING_DONE] = completed }
    }

    suspend fun setFirstDayOfWeek(day: DayOfWeek) {
        dataStore.edit { it[Keys.FIRST_DAY_OF_WEEK] = day.value }
    }
}
