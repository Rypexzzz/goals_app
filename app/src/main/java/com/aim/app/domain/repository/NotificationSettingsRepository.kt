package com.aim.app.domain.repository

import com.aim.app.domain.model.NotificationSettings
import com.aim.app.domain.model.NotificationType
import com.aim.app.domain.model.NotificationTypeSettings
import kotlinx.coroutines.flow.Flow
import java.time.LocalTime

interface NotificationSettingsRepository {

    fun observeSettings(): Flow<NotificationSettings>

    suspend fun getSettings(): NotificationSettings

    suspend fun setMasterEnabled(enabled: Boolean)

    suspend fun setTypeSettings(type: NotificationType, settings: NotificationTypeSettings)

    suspend fun setDoNotDisturb(start: LocalTime?, end: LocalTime?)
}
