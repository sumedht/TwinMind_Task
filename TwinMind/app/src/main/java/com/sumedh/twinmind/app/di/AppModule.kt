package com.sumedh.twinmind.app.di

import android.content.Context
import androidx.room.Room
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.sumedh.twinmind.app.data.db.AppDatabase
import com.sumedh.twinmind.app.data.db.MeetingDao
import com.sumedh.twinmind.app.data.db.TranscriptDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseApp(@ApplicationContext context: Context): FirebaseApp? {
           return FirebaseApp.initializeApp(context)
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(app: FirebaseApp?): FirebaseAuth? {
        // By depending on FirebaseApp, Hilt ensures provideFirebaseApp() is called first.
        if (app != null)
        return FirebaseAuth.getInstance(app)
        else
            return null
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "twinmind-db"
        ).build()
    }

    @Provides
    fun provideMeetingDao(appDatabase: AppDatabase): MeetingDao {
        return appDatabase.meetingDao()
    }

    @Provides
    fun provideTranscriptDao(appDatabase: AppDatabase): TranscriptDao {
        return appDatabase.transcriptDao()
    }

//    @Provides
//    @Singleton
//    fun provideFirebaseAuth(): FirebaseAuth {
//        return FirebaseAuth.getInstance()
//    }

//    @Provides
//    @Singleton
//    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
//        return Room.databaseBuilder(
//            context,
//            AppDatabase::class.java,
//            "twinmind-db"
//        ).build()
//    }
//
//    @Provides
//    fun provideMeetingDao(appDatabase: AppDatabase): MeetingDao {
//        return appDatabase.meetingDao()
//    }
//
//    @Provides
//    fun provideTranscriptDao(appDatabase: AppDatabase): TranscriptDao {
//        return appDatabase.transcriptDao()
//    }

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.openai.com/") // Or your Gemini API base URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

//    @Provides
//    @Singleton
//    fun provideTranscriptionApiService(retrofit: Retrofit): TranscriptionApiService {
//        return retrofit.create(TranscriptionApiService::class.java)
//    }
}