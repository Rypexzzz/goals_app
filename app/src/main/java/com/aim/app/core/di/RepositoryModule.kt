package com.aim.app.core.di

import com.aim.app.data.repository.AppPreferencesRepositoryImpl
import com.aim.app.data.repository.BackupRepositoryImpl
import com.aim.app.data.repository.GoalRepositoryImpl
import com.aim.app.data.repository.HabitRepositoryImpl
import com.aim.app.data.repository.NotificationSettingsRepositoryImpl
import com.aim.app.data.repository.TaskRepositoryImpl
import com.aim.app.data.repository.ThemeRepositoryImpl
import com.aim.app.domain.repository.AppPreferencesRepository
import com.aim.app.domain.repository.BackupRepository
import com.aim.app.domain.repository.GoalRepository
import com.aim.app.domain.repository.HabitRepository
import com.aim.app.domain.repository.NotificationSettingsRepository
import com.aim.app.domain.repository.TaskRepository
import com.aim.app.domain.repository.ThemeRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindThemeRepository(impl: ThemeRepositoryImpl): ThemeRepository

    @Binds
    @Singleton
    abstract fun bindGoalRepository(impl: GoalRepositoryImpl): GoalRepository

    @Binds
    @Singleton
    abstract fun bindTaskRepository(impl: TaskRepositoryImpl): TaskRepository

    @Binds
    @Singleton
    abstract fun bindHabitRepository(impl: HabitRepositoryImpl): HabitRepository

    @Binds
    @Singleton
    abstract fun bindNotificationSettingsRepository(
        impl: NotificationSettingsRepositoryImpl,
    ): NotificationSettingsRepository

    @Binds
    @Singleton
    abstract fun bindAppPreferencesRepository(
        impl: AppPreferencesRepositoryImpl,
    ): AppPreferencesRepository

    @Binds
    @Singleton
    abstract fun bindBackupRepository(impl: BackupRepositoryImpl): BackupRepository
}
