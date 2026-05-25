package com.aim.app.core.notification

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.aim.app.core.alarm.AlarmScheduler
import com.aim.app.core.work.DailyMaintenanceWorker
import com.aim.app.core.work.EveningCheckinWorker
import com.aim.app.core.work.MorningBriefWorker
import com.aim.app.core.work.WeeklySummaryWorker
import com.aim.app.domain.model.NotificationSettings
import com.aim.app.domain.model.NotificationType
import com.aim.app.domain.repository.NotificationSettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Оркестратор уведомлений. Дайджест-типы — через периодические Worker'ы WorkManager;
 * точные ко времени (первое дело, предупреждение стрика) — через AlarmManager.
 * Ежедневное обслуживание (чистка корзины + дедлайны) всегда активно.
 */
@Singleton
class NotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: NotificationSettingsRepository,
    private val alarmScheduler: AlarmScheduler,
) {
    private val workManager = WorkManager.getInstance(context)

    /** Полная (пере)установка всех расписаний — вызывается при изменении настроек и после reboot. */
    suspend fun rescheduleAll() {
        val settings = settingsRepository.getSettings()
        NotificationType.entries.forEach { applyType(it, settings) }
        scheduleDailyMaintenance()
    }

    suspend fun applyType(type: NotificationType) {
        applyType(type, settingsRepository.getSettings())
    }

    private fun applyType(type: NotificationType, settings: NotificationSettings) {
        val active = settings.isActive(type)
        val time = settings.settingsFor(type).time
        when (type) {
            NotificationType.MORNING_BRIEF ->
                scheduleOrCancelDailyWorker<MorningBriefWorker>(MorningBriefWorker.UNIQUE_NAME, active, time)
            NotificationType.EVENING_CHECKIN ->
                scheduleOrCancelDailyWorker<EveningCheckinWorker>(EveningCheckinWorker.UNIQUE_NAME, active, time)
            NotificationType.WEEKLY_SUMMARY ->
                scheduleOrCancelWeeklyWorker<WeeklySummaryWorker>(WeeklySummaryWorker.UNIQUE_NAME, active, time)
            NotificationType.FIRST_THING, NotificationType.STREAK_WARNING -> {
                if (active && time != null) alarmScheduler.scheduleDaily(type, time)
                else alarmScheduler.cancel(type)
            }
            NotificationType.DEADLINE_APPROACHING -> Unit // через DailyMaintenanceWorker
        }
    }

    /** Перепланировать следующее срабатывание алярма (вызывается из AlarmReceiver). */
    suspend fun scheduleNext(type: NotificationType) {
        val settings = settingsRepository.getSettings()
        if (!settings.isActive(type)) {
            alarmScheduler.cancel(type)
            return
        }
        val time = settings.settingsFor(type).time ?: return
        // На следующий день в то же время.
        alarmScheduler.scheduleDaily(type, time, now = LocalDateTime.now().plusMinutes(1))
    }

    private inline fun <reified W : androidx.work.ListenableWorker> scheduleOrCancelDailyWorker(
        uniqueName: String,
        active: Boolean,
        time: LocalTime?,
    ) {
        if (!active || time == null) {
            workManager.cancelUniqueWork(uniqueName)
            return
        }
        val request = PeriodicWorkRequestBuilder<W>(24, TimeUnit.HOURS)
            .setInitialDelay(delayUntilNextDaily(time), TimeUnit.MILLISECONDS)
            .build()
        workManager.enqueueUniquePeriodicWork(uniqueName, ExistingPeriodicWorkPolicy.UPDATE, request)
    }

    private inline fun <reified W : androidx.work.ListenableWorker> scheduleOrCancelWeeklyWorker(
        uniqueName: String,
        active: Boolean,
        time: LocalTime?,
    ) {
        if (!active || time == null) {
            workManager.cancelUniqueWork(uniqueName)
            return
        }
        val request = PeriodicWorkRequestBuilder<W>(7, TimeUnit.DAYS)
            .setInitialDelay(delayUntilNextWeekly(DayOfWeek.SUNDAY, time), TimeUnit.MILLISECONDS)
            .build()
        workManager.enqueueUniquePeriodicWork(uniqueName, ExistingPeriodicWorkPolicy.UPDATE, request)
    }

    private fun scheduleDailyMaintenance() {
        val request = PeriodicWorkRequestBuilder<DailyMaintenanceWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(delayUntilNextDaily(LocalTime.of(3, 0)), TimeUnit.MILLISECONDS)
            .build()
        workManager.enqueueUniquePeriodicWork(
            DailyMaintenanceWorker.UNIQUE_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }

    private fun delayUntilNextDaily(time: LocalTime, now: LocalDateTime = LocalDateTime.now()): Long {
        var target = now.toLocalDate().atTime(time)
        if (!target.isAfter(now)) target = target.plusDays(1)
        return Duration.between(now, target).toMillis()
    }

    private fun delayUntilNextWeekly(
        dayOfWeek: DayOfWeek,
        time: LocalTime,
        now: LocalDateTime = LocalDateTime.now(),
    ): Long {
        var target = now.toLocalDate().with(TemporalAdjusters.nextOrSame(dayOfWeek)).atTime(time)
        if (!target.isAfter(now)) {
            target = now.toLocalDate().with(TemporalAdjusters.next(dayOfWeek)).atTime(time)
        }
        return Duration.between(now, target).toMillis()
    }
}
