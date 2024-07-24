package com.example.orderpractice.data.repository

import com.example.orderpractice.data.dto.EventDto
import com.example.orderpractice.data.remote.TrackingApi
import com.example.orderpractice.domain.repository.TrackingRepository
import javax.inject.Inject

class TrackingRepositoryImpl @Inject constructor(
    private val api: TrackingApi
) : TrackingRepository {
    private var events: List<EventDto> = emptyList()

    override suspend fun getTracking(): List<EventDto> {
        val response = api.getTracking()
        events = response.data.trackings[0].events
        return events
    }
}