package com.twinmind.app.di

import com.twinmind.app.BuildConfig
import com.twinmind.app.network.MockTranscriptionService
import com.twinmind.app.network.TranscriptionService
import com.twinmind.app.network.WhisperTranscriptionService
import com.twinmind.app.network.api.SummaryApi
import com.twinmind.app.network.api.WhisperApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides @Singleton
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                else HttpLoggingInterceptor.Level.NONE
            })
            // NO global auth interceptor here — each service adds its own key per-request
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

    @Provides @Singleton @Named("whisper")
    fun provideWhisperRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://api.openai.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides @Singleton @Named("summary")
    fun provideSummaryRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://api.openai.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides @Singleton
    fun provideWhisperApi(@Named("whisper") retrofit: Retrofit): WhisperApi =
        retrofit.create(WhisperApi::class.java)

    @Provides @Singleton
    fun provideSummaryApi(@Named("summary") retrofit: Retrofit): SummaryApi =
        retrofit.create(SummaryApi::class.java)

    /**
     * Bind the correct TranscriptionService implementation based on build config.
     * Set TRANSCRIPTION_MODE = "mock" in build.gradle for offline/demo use.
     */
    @Provides @Singleton
    fun provideTranscriptionService(
        mockService: MockTranscriptionService,
        whisperService: WhisperTranscriptionService
    ): TranscriptionService =
        if (BuildConfig.TRANSCRIPTION_MODE == "mock") mockService else whisperService
}