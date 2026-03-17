package com.twinmind.app.di

import android.content.Context
import androidx.room.Room
import com.twinmind.app.data.db.TwinMindDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): TwinMindDatabase =
        Room.databaseBuilder(ctx, TwinMindDatabase::class.java, "twinmind.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideSessionDao(db: TwinMindDatabase) = db.recordingSessionDao()
    @Provides fun provideChunkDao(db: TwinMindDatabase)   = db.audioChunkDao()
    @Provides fun provideTranscriptDao(db: TwinMindDatabase) = db.transcriptDao()
    @Provides fun provideSummaryDao(db: TwinMindDatabase) = db.summaryDao()
}