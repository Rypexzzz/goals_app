package com.aim.app.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.content.getSystemService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Создаёт каналы уведомлений при старте приложения. Идемпотентно — повторный вызов
 * безопасен (система игнорирует уже существующие каналы с тем же id).
 */
@Singleton
class NotificationChannelInitializer @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun createChannels() {
        val manager = context.getSystemService<NotificationManager>() ?: return
        AimChannel.entries.forEach { channel ->
            val notificationChannel = NotificationChannel(
                channel.id,
                context.getString(channel.nameRes),
                channel.importance,
            )
            manager.createNotificationChannel(notificationChannel)
        }
    }
}
