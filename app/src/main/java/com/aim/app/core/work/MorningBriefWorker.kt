package com.aim.app.core.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.aim.app.core.notification.NotificationContentProvider
import com.aim.app.domain.model.NotificationType
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class MorningBriefWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val contentProvider: NotificationContentProvider,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        contentProvider.postIfRelevant(NotificationType.MORNING_BRIEF)
        return Result.success()
    }

    companion object {
        const val UNIQUE_NAME = "aim_morning_brief"
    }
}
