package com.aim.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.aim.app.core.notification.NotificationScheduler
import com.aim.app.domain.model.ThemeMode
import com.aim.app.domain.usecase.theme.ObserveThemeModeUseCase
import com.aim.app.presentation.theme.AimTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var observeThemeMode: ObserveThemeModeUseCase

    @Inject
    lateinit var notificationScheduler: NotificationScheduler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Синхронизируем расписание уведомлений с актуальными настройками при запуске.
        lifecycleScope.launch {
            runCatching { notificationScheduler.rescheduleAll() }
        }
        // Периодическое обновление виджета (полночь + каждые 30 мин).
        com.aim.app.widget.WidgetUpdateWorker.enqueue(applicationContext)

        setContent {
            val themeMode by observeThemeMode()
                .collectAsStateWithLifecycle(initialValue = ThemeMode.LIGHT)

            AimTheme(themeMode = themeMode) {
                AimApp()
            }
        }
    }
}
