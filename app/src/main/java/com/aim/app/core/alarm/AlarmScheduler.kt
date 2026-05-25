package com.aim.app.core.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import androidx.core.content.getSystemService
import com.aim.app.core.notification.receiver.AlarmReceiver
import com.aim.app.domain.model.NotificationType
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Обёртка над [AlarmManager] для точных, переживающих Doze, алярмов (README §8.4).
 * Если у приложения нет права на точные алармы (Android 12+), деградирует до неточного.
 */
@Singleton
class AlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val alarmManager = context.getSystemService<AlarmManager>()

    fun canScheduleExact(): Boolean =
        alarmManager?.canScheduleExactAlarms() ?: false

    /** Запланировать ближайшее срабатывание [type] на [time] (сегодня если ещё не прошло, иначе завтра). */
    fun scheduleDaily(type: NotificationType, time: LocalTime, now: java.time.LocalDateTime = java.time.LocalDateTime.now()) {
        val today = now.toLocalDate()
        val target = if (now.toLocalTime() < time) today else today.plusDays(1)
        scheduleAt(type, target, time)
    }

    fun scheduleAt(type: NotificationType, date: LocalDate, time: LocalTime) {
        val manager = alarmManager ?: return
        val triggerAtMillis = date.atTime(time)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val pendingIntent = pendingIntent(type)
        try {
            if (manager.canScheduleExactAlarms()) {
                manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            } else {
                manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
        } catch (e: SecurityException) {
            Timber.w(e, "Failed to schedule exact alarm for $type")
            manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    fun cancel(type: NotificationType) {
        alarmManager?.cancel(pendingIntent(type))
    }

    private fun pendingIntent(type: NotificationType): PendingIntent {
        val intent = AlarmReceiver.intent(context, type)
        return PendingIntent.getBroadcast(
            context,
            2000 + type.ordinal,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
