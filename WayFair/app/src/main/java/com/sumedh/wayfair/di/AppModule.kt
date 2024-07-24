package com.sumedh.wayfair.di

import com.sumedh.wayfair.common.Constants
import com.sumedh.wayfair.data.remote.ProductApi
import com.sumedh.wayfair.data.repository.ProductsRepositoryImpl
import com.sumedh.wayfair.domain.repository.ProductsRepository
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
    fun provideProductApi() : ProductApi {
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(OkHttpClient())
            .build()
            .create(ProductApi::class.java)
    }

    @Provides
    @Singleton
    fun provideProductRepository(api: ProductApi) : ProductsRepository {
        return ProductsRepositoryImpl(api)
    }
}