package com.aim.app.core.di

import com.aim.app.data.repository.GoalRepositoryImpl
import com.aim.app.data.repository.TaskRepositoryImpl
import com.aim.app.data.repository.ThemeRepositoryImpl
import com.aim.app.domain.repository.GoalRepository
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
}
