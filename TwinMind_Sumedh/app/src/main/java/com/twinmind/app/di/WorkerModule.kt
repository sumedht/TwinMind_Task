package com.twinmind.app.di

import androidx.hilt.work.HiltWorkerFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WorkerModule
// HiltWorkerFactory is auto-provided by the hilt-work artifact.
// Workers use @AssistedInject — no manual @Provides needed.