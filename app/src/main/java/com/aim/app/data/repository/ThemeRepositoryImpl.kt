package com.aim.app.data.repository

import com.aim.app.data.local.preferences.ThemePreferencesDataSource
import com.aim.app.domain.model.ThemeMode
import com.aim.app.domain.repository.ThemeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ThemeRepositoryImpl @Inject constructor(
    private val source: ThemePreferencesDataSource,
) : ThemeRepository {
    override fun observeThemeMode(): Flow<ThemeMode> = source.themeMode
    override suspend fun setThemeMode(mode: ThemeMode) = source.setThemeMode(mode)
}
