package com.aim.app.presentation.screens.settings

import com.aim.app.domain.model.ThemeMode

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.LIGHT,
)
