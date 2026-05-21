package com.aim.app.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.aim.app.domain.model.ThemeMode

@Composable
fun AimTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val systemDark = isSystemInDarkTheme()
    val useDark = when (themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }
    val colorScheme = if (useDark) AimDarkColorScheme else AimLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AimTypography,
        shapes = AimShapes,
        content = content,
    )
}
