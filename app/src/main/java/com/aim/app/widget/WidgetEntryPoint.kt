package com.aim.app.widget

import com.aim.app.domain.usecase.today.GetTodayItemsUseCase
import com.aim.app.domain.usecase.today.ToggleTodayItemUseCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Мост Hilt → Glance. `GlanceAppWidget` и `ActionCallback` не являются Hilt-компонентами,
 * поэтому зависимости достаём через EntryPoint от Application-контекста.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun getTodayItemsUseCase(): GetTodayItemsUseCase
    fun toggleTodayItemUseCase(): ToggleTodayItemUseCase
}
