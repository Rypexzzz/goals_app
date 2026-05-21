package com.aim.app.presentation.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

private val LightPrimary = Color(0xFFEC6A3C)
private val LightOnPrimary = Color(0xFFFFFFFF)
private val LightPrimaryContainer = Color(0xFFFFE0D4)
private val LightOnPrimaryContainer = Color(0xFF3A1602)
private val LightSecondary = Color(0xFF3D5A6C)
private val LightOnSecondary = Color(0xFFFFFFFF)
private val LightSecondaryContainer = Color(0xFFD3E4F1)
private val LightOnSecondaryContainer = Color(0xFF0E1E27)
private val LightTertiary = Color(0xFF7B6F50)
private val LightOnTertiary = Color(0xFFFFFFFF)
private val LightTertiaryContainer = Color(0xFFF1E6CB)
private val LightOnTertiaryContainer = Color(0xFF2A2210)
private val LightBackground = Color(0xFFFAF7F2)
private val LightOnBackground = Color(0xFF1F1B16)
private val LightSurface = Color(0xFFFFFFFF)
private val LightOnSurface = Color(0xFF1F1B16)
private val LightSurfaceVariant = Color(0xFFF2EDE5)
private val LightOnSurfaceVariant = Color(0xFF55514A)
private val LightOutline = Color(0xFFD4CCC0)
private val LightOutlineVariant = Color(0xFFE8E2D6)
private val LightError = Color(0xFFBA1A1A)
private val LightOnError = Color(0xFFFFFFFF)
private val LightErrorContainer = Color(0xFFFFDAD6)
private val LightOnErrorContainer = Color(0xFF410002)
private val LightScrim = Color(0xFF000000)

private val DarkPrimary = Color(0xFFFF8A60)
private val DarkOnPrimary = Color(0xFF1F1B16)
private val DarkPrimaryContainer = Color(0xFF5A2A14)
private val DarkOnPrimaryContainer = Color(0xFFFFE0D4)
private val DarkSecondary = Color(0xFFA8C0CE)
private val DarkOnSecondary = Color(0xFF142231)
private val DarkSecondaryContainer = Color(0xFF2A3F4F)
private val DarkOnSecondaryContainer = Color(0xFFD3E4F1)
private val DarkTertiary = Color(0xFFD4C5A0)
private val DarkOnTertiary = Color(0xFF2A2210)
private val DarkTertiaryContainer = Color(0xFF4A4128)
private val DarkOnTertiaryContainer = Color(0xFFF1E6CB)
private val DarkBackground = Color(0xFF12110F)
private val DarkOnBackground = Color(0xFFEFEAE0)
private val DarkSurface = Color(0xFF1A1815)
private val DarkOnSurface = Color(0xFFEFEAE0)
private val DarkSurfaceVariant = Color(0xFF24211D)
private val DarkOnSurfaceVariant = Color(0xFFB4AEA3)
private val DarkOutline = Color(0xFF3D3833)
private val DarkOutlineVariant = Color(0xFF2A2724)
private val DarkError = Color(0xFFFFB4AB)
private val DarkOnError = Color(0xFF690005)
private val DarkErrorContainer = Color(0xFF93000A)
private val DarkOnErrorContainer = Color(0xFFFFDAD6)
private val DarkScrim = Color(0xFF000000)

val AimLightColorScheme: ColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    tertiary = LightTertiary,
    onTertiary = LightOnTertiary,
    tertiaryContainer = LightTertiaryContainer,
    onTertiaryContainer = LightOnTertiaryContainer,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
    error = LightError,
    onError = LightOnError,
    errorContainer = LightErrorContainer,
    onErrorContainer = LightOnErrorContainer,
    scrim = LightScrim,
)

val AimDarkColorScheme: ColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    error = DarkError,
    onError = DarkOnError,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer,
    scrim = DarkScrim,
)
