package com.sumedh.practiceproject.di

import com.sumedh.practiceproject.common.Constants
import com.sumedh.practiceproject.data.remote.TodoApi
import com.sumedh.practiceproject.data.repository.TodoListRepositoryImpl
import com.sumedh.practiceproject.domain.repository.TodoListRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideTodoListingApi(): TodoApi {
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TodoApi::class.java)
    }

    @Provides
    fun provideTodoListRepository(api: TodoApi): TodoListRepository {
        return TodoListRepositoryImpl(api)
    }
}