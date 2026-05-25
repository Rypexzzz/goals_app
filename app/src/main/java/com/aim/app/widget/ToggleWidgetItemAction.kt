package com.aim.app.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import dagger.hilt.android.EntryPointAccessors

/**
 * Отмечает выполнение элемента «Сегодня» прямо из виджета и сразу обновляет его.
 */
class ToggleWidgetItemAction : ActionCallback {

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val key = parameters[itemKeyParam] ?: return
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WidgetEntryPoint::class.java,
        )
        entryPoint.toggleTodayItemUseCase().invoke(key)
        TodayWidget().update(context, glanceId)
    }

    companion object {
        val itemKeyParam = ActionParameters.Key<String>("item_key")
    }
}
