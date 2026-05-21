package com.aim.app.domain.repository

import com.aim.app.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface ThemeRepository {
    fun observeThemeMode(): Flow<ThemeMode>
    suspend fun setThemeMode(mode: ThemeMode)
}
