package com.aim.app.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Instant

/**
 * Источник «текущего времени» для репозиториев и use case'ов.
 *
 * Многие классы принимают `clock: () -> Instant` ради тестируемости (в тестах передаётся
 * фиксированное время). Kotlin-дефолт `= Instant::now` Hilt игнорирует, поэтому биндинг нужен явно.
 * `@JvmSuppressWildcards` обязателен: без него тип провайдера станет `Function0<? extends Instant>`
 * и не совпадёт с точками инъекции `Function0<Instant>`.
 */
@Module
@InstallIn(SingletonComponent::class)
object ClockModule {

    @Provides
    fun provideClock(): @JvmSuppressWildcards () -> Instant = Instant::now
}
