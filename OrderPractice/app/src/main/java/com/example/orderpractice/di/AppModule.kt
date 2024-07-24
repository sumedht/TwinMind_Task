package com.example.orderpractice.di

import com.example.orderpractice.common.Constants
import com.example.orderpractice.data.remote.TrackingApi
import com.example.orderpractice.data.repository.TrackingRepositoryImpl
import com.example.orderpractice.domain.repository.TrackingRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideTrackingApi() : TrackingApi {
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(OkHttpClient.Builder().addInterceptor { chain ->
                val request = chain.request().newBuilder().addHeader("Authorization", "Bearer apik_soIKea0VRy6o8mplaLFxPRrGoqhDY4").build()
                chain.proceed(request = request)
            }.build())
            .build()
            .create(TrackingApi::class.java)
        }

    @Provides
    @Singleton
    fun provideTrackingRepository(api: TrackingApi) : TrackingRepository {
        return TrackingRepositoryImpl(api)
    }
}