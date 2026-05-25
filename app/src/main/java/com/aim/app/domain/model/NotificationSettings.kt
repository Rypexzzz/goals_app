package com.aim.app.domain.model

import java.time.LocalTime

/**
 * Настройки одного типа уведомления.
 */
data class NotificationTypeSettings(
    val enabled: Boolean,
    val time: LocalTime?,
    val withSound: Boolean = true,
    val withVibration: Boolean = true,
)

/**
 * Полные настройки уведомлений приложения.
 */
data class NotificationSettings(
    val masterEnabled: Boolean = true,
    val perType: Map<NotificationType, NotificationTypeSettings> = defaults(),
    val doNotDisturbStart: LocalTime? = null,
    val doNotDisturbEnd: LocalTime? = null,
) {
    fun settingsFor(type: NotificationType): NotificationTypeSettings =
        perType[type] ?: defaultFor(type)

    fun isActive(type: NotificationType): Boolean =
        masterEnabled && settingsFor(type).enabled

    /** Попадает ли момент в окно «не беспокоить». Корректно обрабатывает окно через полночь. */
    fun isWithinDoNotDisturb(time: LocalTime): Boolean {
        val start = doNotDisturbStart ?: return false
        val end = doNotDisturbEnd ?: return false
        return if (start <= end) {
            time >= start && time < end
        } else {
            // окно через полночь, напр. 22:00–07:00
            time >= start || time < end
        }
    }

    companion object {
        fun defaultFor(type: NotificationType): NotificationTypeSettings = when (type) {
            NotificationType.MORNING_BRIEF -> NotificationTypeSettings(true, LocalTime.of(8, 0))
            NotificationType.FIRST_THING -> NotificationTypeSettings(false, LocalTime.of(9, 0))
            NotificationType.EVENING_CHECKIN -> NotificationTypeSettings(true, LocalTime.of(21, 30))
            NotificationType.STREAK_WARNING -> NotificationTypeSettings(true, LocalTime.of(22, 30))
            NotificationType.WEEKLY_SUMMARY -> NotificationTypeSettings(true, LocalTime.of(20, 0))
            NotificationType.DEADLINE_APPROACHING -> NotificationTypeSettings(true, null)
        }

        fun defaults(): Map<NotificationType, NotificationTypeSettings> =
            NotificationType.entries.associateWith { defaultFor(it) }
    }
}
