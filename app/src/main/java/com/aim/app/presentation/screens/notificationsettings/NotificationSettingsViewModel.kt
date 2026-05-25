package com.aim.app.presentation.screens.notificationsettings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aim.app.core.notification.NotificationScheduler
import com.aim.app.domain.model.NotificationSettings
import com.aim.app.domain.model.NotificationType
import com.aim.app.domain.usecase.notification.ObserveNotificationSettingsUseCase
import com.aim.app.domain.usecase.notification.UpdateNotificationSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

data class NotificationSettingsUiState(
    val isLoading: Boolean = true,
    val settings: NotificationSettings = NotificationSettings(),
)

@HiltViewModel
class NotificationSettingsViewModel @Inject constructor(
    observeSettings: ObserveNotificationSettingsUseCase,
    private val update: UpdateNotificationSettingsUseCase,
    private val scheduler: NotificationScheduler,
) : ViewModel() {

    val uiState: StateFlow<NotificationSettingsUiState> = observeSettings()
        .map { NotificationSettingsUiState(isLoading = false, settings = it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = NotificationSettingsUiState(),
        )

    fun onMasterToggle(enabled: Boolean) = viewModelScope.launch {
        update.setMasterEnabled(enabled)
        scheduler.rescheduleAll()
    }

    fun onTypeToggle(type: NotificationType, enabled: Boolean) = viewModelScope.launch {
        val current = uiState.value.settings.settingsFor(type)
        update.setType(type, current.copy(enabled = enabled))
        scheduler.applyType(type)
    }

    fun onTypeTime(type: NotificationType, time: LocalTime) = viewModelScope.launch {
        val current = uiState.value.settings.settingsFor(type)
        update.setType(type, current.copy(time = time))
        scheduler.applyType(type)
    }

    fun onTypeSound(type: NotificationType, withSound: Boolean) = viewModelScope.launch {
        val current = uiState.value.settings.settingsFor(type)
        update.setType(type, current.copy(withSound = withSound))
    }

    fun onTypeVibration(type: NotificationType, withVibration: Boolean) = viewModelScope.launch {
        val current = uiState.value.settings.settingsFor(type)
        update.setType(type, current.copy(withVibration = withVibration))
    }

    fun onDoNotDisturbChange(start: LocalTime?, end: LocalTime?) = viewModelScope.launch {
        update.setDoNotDisturb(start, end)
    }
}
