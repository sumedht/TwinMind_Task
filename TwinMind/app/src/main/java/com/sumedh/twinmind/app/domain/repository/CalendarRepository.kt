package com.sumedh.twinmind.app.domain.repository

import com.sumedh.twinmind.app.data.model.UpcomingEvent
import com.sumedh.twinmind.app.util.Result

interface CalendarRepository {
    /**
     * Fetches a list of upcoming events for the currently signed-in user.
     */
    suspend fun getUpcomingEvents(): Result<List<UpcomingEvent>>
}