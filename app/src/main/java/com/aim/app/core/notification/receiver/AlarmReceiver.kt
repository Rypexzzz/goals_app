package com.aim.app.core.notification.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.aim.app.core.notification.NotificationContentProvider
import com.aim.app.core.notification.NotificationScheduler
import com.aim.app.domain.model.NotificationType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Срабатывает по точному алярму ([com.aim.app.core.alarm.AlarmScheduler]). Строит и постит
 * уведомление, затем планирует следующее срабатывание этого же типа.
 */
@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var contentProvider: NotificationContentProvider

    @Inject
    lateinit var scheduler: NotificationScheduler

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onReceive(context: Context, intent: Intent) {
        val typeName = intent.getStringExtra(EXTRA_TYPE) ?: return
        val type = runCatching { NotificationType.valueOf(typeName) }.getOrNull() ?: return
        val pending = goAsync()
        scope.launch {
            try {
                contentProvider.postIfRelevant(type)
                scheduler.scheduleNext(type)
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val EXTRA_TYPE = "extra_notification_type"

        fun intent(context: Context, type: NotificationType): Intent =
            Intent(context, AlarmReceiver::class.java).apply {
                putExtra(EXTRA_TYPE, type.name)
            }
    }
}
