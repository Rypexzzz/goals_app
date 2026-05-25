package com.aim.app.core.notification.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.aim.app.domain.usecase.task.CompleteTaskUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Обрабатывает действие «Выполнено» из уведомления: отмечает задачу и снимает уведомление —
 * без открытия приложения (README §8.4).
 */
@AndroidEntryPoint
class NotificationActionReceiver : BroadcastReceiver() {

    @Inject
    lateinit var completeTask: CompleteTaskUseCase

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_COMPLETE_TASK -> {
                val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
                val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
                if (taskId <= 0) return
                val pending = goAsync()
                scope.launch {
                    try {
                        completeTask(taskId)
                        if (notificationId >= 0) {
                            NotificationManagerCompat.from(context).cancel(notificationId)
                        }
                    } finally {
                        pending.finish()
                    }
                }
            }
        }
    }

    companion object {
        private const val ACTION_COMPLETE_TASK = "com.aim.app.action.COMPLETE_TASK"
        private const val EXTRA_TASK_ID = "extra_task_id"
        private const val EXTRA_NOTIFICATION_ID = "extra_notification_id"

        fun completeTaskIntent(context: Context, taskId: Long, notificationId: Int): Intent =
            Intent(context, NotificationActionReceiver::class.java).apply {
                action = ACTION_COMPLETE_TASK
                putExtra(EXTRA_TASK_ID, taskId)
                putExtra(EXTRA_NOTIFICATION_ID, notificationId)
            }
    }
}
