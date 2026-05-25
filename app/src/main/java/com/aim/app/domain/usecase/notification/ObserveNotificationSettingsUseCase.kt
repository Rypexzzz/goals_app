package com.aim.app.domain.usecase.notification

import com.aim.app.domain.model.NotificationSettings
import com.aim.app.domain.repository.NotificationSettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveNotificationSettingsUseCase @Inject constructor(
    private val repository: NotificationSettingsRepository,
) {
    operator fun invoke(): Flow<NotificationSettings> = repository.observeSettings()
}
