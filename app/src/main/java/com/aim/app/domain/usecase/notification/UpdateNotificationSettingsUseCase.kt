package com.aim.app.domain.usecase.notification

import com.aim.app.domain.model.NotificationType
import com.aim.app.domain.model.NotificationTypeSettings
import com.aim.app.domain.repository.NotificationSettingsRepository
import java.time.LocalTime
import javax.inject.Inject

/**
 * Точечные обновления настроек уведомлений. Каждый метод — отдельная доменная операция,
 * но они сгруппированы в один класс по теме (изменения одной сущности настроек).
 */
class UpdateNotificationSettingsUseCase @Inject constructor(
    private val repository: NotificationSettingsRepository,
) {
    suspend fun setMasterEnabled(enabled: Boolean) =
        repository.setMasterEnabled(enabled)

    suspend fun setType(type: NotificationType, settings: NotificationTypeSettings) =
        repository.setTypeSettings(type, settings)

    suspend fun setDoNotDisturb(start: LocalTime?, end: LocalTime?) =
        repository.setDoNotDisturb(start, end)
}
