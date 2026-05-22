package com.aim.app.core.di

import android.content.Context
import androidx.room.Room
import com.aim.app.data.local.db.AimDatabase
import com.aim.app.data.local.db.GoalDao
import com.aim.app.data.local.db.TaskDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAimDatabase(
        @ApplicationContext context: Context,
    ): AimDatabase = Room.databaseBuilder(
        context = context,
        klass = AimDatabase::class.java,
        name = AimDatabase.NAME,
    ).build()

    @Provides
    fun provideGoalDao(db: AimDatabase): GoalDao = db.goalDao()

    @Provides
    fun provideTaskDao(db: AimDatabase): TaskDao = db.taskDao()
}
