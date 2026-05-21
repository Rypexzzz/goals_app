package com.aim.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aim.app.domain.model.ThemeMode
import com.aim.app.domain.usecase.theme.ObserveThemeModeUseCase
import com.aim.app.presentation.theme.AimTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var observeThemeMode: ObserveThemeModeUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by observeThemeMode()
                .collectAsStateWithLifecycle(initialValue = ThemeMode.LIGHT)

            AimTheme(themeMode = themeMode) {
                AimApp()
            }
        }
    }
}
