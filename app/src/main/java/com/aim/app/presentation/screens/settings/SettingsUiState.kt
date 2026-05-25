package com.aim.app.presentation.screens.settings

import com.aim.app.domain.model.ThemeMode
import java.time.DayOfWeek

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.LIGHT,
    val firstDayOfWeek: DayOfWeek = DayOfWeek.MONDAY,
)
