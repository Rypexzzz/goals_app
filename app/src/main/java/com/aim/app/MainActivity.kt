package com.aim.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.aim.app.core.notification.NotificationScheduler
import com.aim.app.presentation.RootViewModel
import com.aim.app.presentation.screens.onboarding.OnboardingScreen
import com.aim.app.presentation.theme.AimTheme
import com.aim.app.widget.TodayWidgetSync
import com.aim.app.widget.WidgetUpdateWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var notificationScheduler: NotificationScheduler

    @Inject
    lateinit var todayWidgetSync: TodayWidgetSync

    private val rootViewModel: RootViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Синхронизируем расписание уведомлений с актуальными настройками при запуске.
        lifecycleScope.launch {
            runCatching { notificationScheduler.rescheduleAll() }
        }
        // Периодическое обновление виджета (полночь + каждые 30 мин).
        WidgetUpdateWorker.enqueue(applicationContext)
        // Пока приложение на переднем плане — мгновенно отражаем изменения в виджете.
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                todayWidgetSync.keepInSync()
            }
        }

        setContent {
            val themeMode by rootViewModel.themeMode.collectAsStateWithLifecycle()
            val onboardingCompleted by rootViewModel.onboardingCompleted.collectAsStateWithLifecycle()

            AimTheme(themeMode = themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    when (onboardingCompleted) {
                        null -> Box(modifier = Modifier.fillMaxSize()) // сплэш, пока грузится флаг
                        false -> OnboardingScreen(onFinished = {})
                        true -> AimApp()
                    }
                }
            }
        }
    }
}
