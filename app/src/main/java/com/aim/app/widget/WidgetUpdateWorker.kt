package com.aim.app.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Периодически обновляет виджет (каждые 30 минут) — пересчёт списка «сегодня»,
 * в т.ч. смена дня в полночь (README §9.3).
 */
@HiltWorker
class WidgetUpdateWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = try {
        TodayWidget().updateAll(applicationContext)
        Result.success()
    } catch (e: Exception) {
        Timber.e(e, "Widget update failed")
        Result.retry()
    }

    companion object {
        private const val UNIQUE_NAME = "aim_widget_update"

        fun enqueue(context: Context) {
            val request = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(30, TimeUnit.MINUTES).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        }
    }
}
