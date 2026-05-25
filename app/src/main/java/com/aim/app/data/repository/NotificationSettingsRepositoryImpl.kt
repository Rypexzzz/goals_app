package com.aim.app.data.repository

import com.aim.app.data.local.preferences.NotificationPreferencesDataSource
import com.aim.app.domain.model.NotificationSettings
import com.aim.app.domain.model.NotificationType
import com.aim.app.domain.model.NotificationTypeSettings
import com.aim.app.domain.repository.NotificationSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationSettingsRepositoryImpl @Inject constructor(
    private val source: NotificationPreferencesDataSource,
) : NotificationSettingsRepository {

    override fun observeSettings(): Flow<NotificationSettings> = source.settings

    override suspend fun getSettings(): NotificationSettings = source.settings.first()

    override suspend fun setMasterEnabled(enabled: Boolean) {
        source.update { it.copy(masterEnabled = enabled) }
    }

    override suspend fun setTypeSettings(type: NotificationType, settings: NotificationTypeSettings) {
        source.update { it.copy(perType = it.perType + (type to settings)) }
    }

    override suspend fun setDoNotDisturb(start: LocalTime?, end: LocalTime?) {
        source.update { it.copy(doNotDisturbStart = start, doNotDisturbEnd = end) }
    }
}
