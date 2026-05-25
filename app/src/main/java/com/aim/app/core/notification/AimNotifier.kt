package com.aim.app.core.notification

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.aim.app.MainActivity
import com.aim.app.R
import com.aim.app.core.notification.receiver.NotificationActionReceiver
import com.aim.app.domain.model.NotificationType
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Обёртка над [NotificationManagerCompat] — строит и постит уведомления для каждого типа.
 * Перед постингом проверяет разрешение POST_NOTIFICATIONS (Android 13+).
 */
@Singleton
class AimNotifier @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val manager = NotificationManagerCompat.from(context)

    fun canPostNotifications(): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission") // защищено проверкой canPostNotifications()
    fun notify(
        type: NotificationType,
        title: String,
        text: String,
        withSound: Boolean = true,
        withVibration: Boolean = true,
        completeTaskActionId: Long? = null,
    ) {
        if (!canPostNotifications()) return

        val channel = AimChannel.forType(type)
        val builder = NotificationCompat.Builder(context, channel.id)
            .setSmallIcon(R.drawable.ic_stat_aim)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setAutoCancel(true)
            .setContentIntent(openAppIntent())
            .setOnlyAlertOnce(true)

        if (!withSound && !withVibration) {
            builder.setSilent(true)
        }

        if (completeTaskActionId != null) {
            builder.addAction(
                R.drawable.ic_stat_aim,
                context.getString(R.string.notification_action_done),
                completeTaskIntent(completeTaskActionId, type),
            )
        }

        runCatching { manager.notify(type.notificationId(), builder.build()) }
    }

    fun cancel(type: NotificationType) {
        manager.cancel(type.notificationId())
    }

    private fun openAppIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun completeTaskIntent(taskId: Long, type: NotificationType): PendingIntent {
        val intent = NotificationActionReceiver.completeTaskIntent(context, taskId, type.notificationId())
        return PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}

/** Стабильный id уведомления на тип, чтобы повторный пост заменял предыдущий. */
fun NotificationType.notificationId(): Int = 1000 + ordinal
