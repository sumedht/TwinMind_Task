package com.example.orderpractice.domain.repository

import com.example.orderpractice.data.dto.EventDto

interface TrackingRepository {

    suspend fun getTracking(): List<EventDto>
}