package com.aim.app.core.notification

import android.content.Context
import com.aim.app.R
import com.aim.app.domain.model.GoalFilter
import com.aim.app.domain.model.NotificationType
import com.aim.app.domain.model.TodayItem
import com.aim.app.domain.repository.GoalRepository
import com.aim.app.domain.repository.NotificationSettingsRepository
import com.aim.app.domain.usecase.today.GetTodayItemsUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Собирает контент уведомления из доменных данных и постит его, если тип активен и
 * момент не попадает в окно «не беспокоить».
 */
@Singleton
class NotificationContentProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notifier: AimNotifier,
    private val settingsRepository: NotificationSettingsRepository,
    private val goalRepository: GoalRepository,
    private val getTodayItems: GetTodayItemsUseCase,
) {
    suspend fun postIfRelevant(type: NotificationType, now: LocalTime = LocalTime.now()) {
        val settings = settingsRepository.getSettings()
        if (!settings.isActive(type)) return
        if (settings.isWithinDoNotDisturb(now)) return
        val typeSettings = settings.settingsFor(type)

        when (type) {
            NotificationType.MORNING_BRIEF -> postMorningBrief(typeSettings.withSound, typeSettings.withVibration)
            NotificationType.FIRST_THING -> postFirstThing(typeSettings.withSound, typeSettings.withVibration)
            NotificationType.EVENING_CHECKIN -> postEveningCheckin(typeSettings.withSound, typeSettings.withVibration)
            NotificationType.STREAK_WARNING -> postStreakWarning(typeSettings.withSound, typeSettings.withVibration)
            NotificationType.WEEKLY_SUMMARY -> postWeeklySummary(typeSettings.withSound, typeSettings.withVibration)
            NotificationType.DEADLINE_APPROACHING -> postDeadlineApproaching(typeSettings.withSound, typeSettings.withVibration)
        }
    }

    private suspend fun postMorningBrief(sound: Boolean, vibration: Boolean) {
        val snapshot = getTodayItems().first()
        val taskCount = snapshot.todo.count { it is TodayItem.TaskItem } +
            snapshot.doneToday.count { it is TodayItem.TaskItem }
        val habitCount = snapshot.todo.count { it is TodayItem.HabitItem } +
            snapshot.doneToday.count { it is TodayItem.HabitItem }
        notifier.notify(
            type = NotificationType.MORNING_BRIEF,
            title = context.getString(R.string.notif_morning_title),
            text = context.getString(R.string.notif_morning_text, taskCount, habitCount),
            withSound = sound,
            withVibration = vibration,
        )
    }

    private suspend fun postFirstThing(sound: Boolean, vibration: Boolean) {
        val snapshot = getTodayItems().first()
        val firstTask = snapshot.todo.firstOrNull { it is TodayItem.TaskItem } ?: return
        notifier.notify(
            type = NotificationType.FIRST_THING,
            title = context.getString(R.string.notif_first_thing_title),
            text = firstTask.title,
            withSound = sound,
            withVibration = vibration,
            completeTaskActionId = (firstTask as? TodayItem.TaskItem)
                ?.takeIf { !it.isRecurringInstance }?.task?.id,
        )
    }

    private fun postEveningCheckin(sound: Boolean, vibration: Boolean) {
        notifier.notify(
            type = NotificationType.EVENING_CHECKIN,
            title = context.getString(R.string.notif_evening_title),
            text = context.getString(R.string.notif_evening_text),
            withSound = sound,
            withVibration = vibration,
        )
    }

    private suspend fun postStreakWarning(sound: Boolean, vibration: Boolean) {
        val snapshot = getTodayItems().first()
        val atRisk = snapshot.todo
            .filterIsInstance<TodayItem.HabitItem>()
            .filter { it.currentStreak >= STREAK_RISK_THRESHOLD && it.status == null }
        if (atRisk.isEmpty()) return
        val habit = atRisk.maxBy { it.currentStreak }
        notifier.notify(
            type = NotificationType.STREAK_WARNING,
            title = context.getString(R.string.notif_streak_title),
            text = context.getString(R.string.notif_streak_text, habit.title, habit.currentStreak),
            withSound = sound,
            withVibration = vibration,
        )
    }

    private suspend fun postWeeklySummary(sound: Boolean, vibration: Boolean) {
        val snapshot = getTodayItems().first()
        notifier.notify(
            type = NotificationType.WEEKLY_SUMMARY,
            title = context.getString(R.string.notif_weekly_title),
            text = context.getString(R.string.notif_weekly_text, snapshot.doneCount),
            withSound = sound,
            withVibration = vibration,
        )
    }

    private suspend fun postDeadlineApproaching(sound: Boolean, vibration: Boolean, today: LocalDate = LocalDate.now()) {
        val goals = goalRepository.observeGoals(GoalFilter.ACTIVE).first()
        val soon = goals.mapNotNull { goal ->
            val deadline = goal.deadline ?: return@mapNotNull null
            val daysLeft = ChronoUnit.DAYS.between(today, deadline)
            if (daysLeft in DEADLINE_DAYS) goal to daysLeft else null
        }.minByOrNull { it.second } ?: return
        notifier.notify(
            type = NotificationType.DEADLINE_APPROACHING,
            title = context.getString(R.string.notif_deadline_title),
            text = context.getString(R.string.notif_deadline_text, soon.first.title, soon.second.toInt()),
            withSound = sound,
            withVibration = vibration,
        )
    }

    private companion object {
        const val STREAK_RISK_THRESHOLD = 7
        val DEADLINE_DAYS = listOf(1L, 3L)
    }
}
