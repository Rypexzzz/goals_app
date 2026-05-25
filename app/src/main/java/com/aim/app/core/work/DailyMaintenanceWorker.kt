package com.aim.app.core.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.aim.app.core.notification.NotificationContentProvider
import com.aim.app.domain.model.NotificationType
import com.aim.app.domain.usecase.maintenance.PurgeOldTrashUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

/**
 * Ежедневное обслуживание: чистка корзины (записи старше 30 дней) и уведомление
 * о приближающихся дедлайнах.
 */
@HiltWorker
class DailyMaintenanceWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val purgeOldTrash: PurgeOldTrashUseCase,
    private val contentProvider: NotificationContentProvider,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            purgeOldTrash()
            contentProvider.postIfRelevant(NotificationType.DEADLINE_APPROACHING)
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Daily maintenance failed")
            Result.retry()
        }
    }

    companion object {
        const val UNIQUE_NAME = "aim_daily_maintenance"
    }
}
