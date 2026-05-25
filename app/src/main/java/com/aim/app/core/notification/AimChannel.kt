package com.aim.app.core.notification

import android.app.NotificationManager
import androidx.annotation.StringRes
import com.aim.app.R
import com.aim.app.domain.model.NotificationType

/**
 * Каналы уведомлений (README §8.3).
 */
enum class AimChannel(
    val id: String,
    @StringRes val nameRes: Int,
    val importance: Int,
) {
    DAILY("aim_daily", R.string.channel_daily, NotificationManager.IMPORTANCE_DEFAULT),
    TASKS("aim_tasks", R.string.channel_tasks, NotificationManager.IMPORTANCE_HIGH),
    HABITS("aim_habits", R.string.channel_habits, NotificationManager.IMPORTANCE_DEFAULT),
    SUMMARY("aim_summary", R.string.channel_summary, NotificationManager.IMPORTANCE_LOW),
    ;

    companion object {
        fun forType(type: NotificationType): AimChannel = when (type) {
            NotificationType.MORNING_BRIEF -> DAILY
            NotificationType.FIRST_THING -> TASKS
            NotificationType.EVENING_CHECKIN -> HABITS
            NotificationType.STREAK_WARNING -> HABITS
            NotificationType.WEEKLY_SUMMARY -> SUMMARY
            NotificationType.DEADLINE_APPROACHING -> TASKS
        }
    }
}
