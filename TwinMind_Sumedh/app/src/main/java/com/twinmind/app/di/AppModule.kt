package com.twinmind.app.di

import android.content.Context
import com.twinmind.app.work.RealWorkScheduler
import com.twinmind.app.work.WorkScheduler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideWorkScheduler(
        @ApplicationContext context: Context
    ): WorkScheduler = RealWorkScheduler(context)
}