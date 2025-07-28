package com.sumedh.twinmind.app.di

import com.sumedh.twinmind.app.data.repository.AuthRepositoryImpl
import com.sumedh.twinmind.app.data.repository.CalendarRepositoryImpl
import com.sumedh.twinmind.app.data.repository.MeetingRepositoryImpl
import com.sumedh.twinmind.app.domain.repository.AuthRepository
import com.sumedh.twinmind.app.domain.repository.CalendarRepository
import com.sumedh.twinmind.app.domain.repository.MeetingRepository
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
    abstract fun bindMeetingRepository(
        meetingRepositoryImpl: MeetingRepositoryImpl
    ): MeetingRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindCalendarRepository(
        calendarRepository: CalendarRepositoryImpl
    ): CalendarRepository
}