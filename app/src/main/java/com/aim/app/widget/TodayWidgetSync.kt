package com.aim.app.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.aim.app.domain.usecase.today.GetTodayItemsUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Держит виджет «Сегодня» в синхроне с приложением, пока оно на переднем плане: при любом
 * изменении видимых элементов (отметка/редактирование/удаление задач и привычек) вызывает
 * [TodayWidget.updateAll]. В фоне обновлением занимается [WidgetUpdateWorker].
 */
@Singleton
class TodayWidgetSync @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getTodayItems: GetTodayItemsUseCase,
) {
    /** Вызывать из foreground-scope (например, через `repeatOnLifecycle(STARTED)`). */
    suspend fun keepInSync() {
        getTodayItems()
            .map { it.todo + it.doneToday }
            .distinctUntilChanged()
            .drop(1) // первый эмит — текущее состояние, виджет уже актуален
            .collect { TodayWidget().updateAll(context) }
    }
}
