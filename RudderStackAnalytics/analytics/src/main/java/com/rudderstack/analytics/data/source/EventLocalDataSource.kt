package com.rudderstack.analytics.data.source

import android.content.Context
import com.rudderstack.analytics.data.model.Event

internal class EventLocalDataSource(context: Context) {
    private val eventDao = EventDatabase.getInstance(context).eventDao()

    suspend fun saveEvent(event: Event) {
        eventDao.insertEvent(event)
    }

    suspend fun deleteEvent(event: Event) {
        eventDao.deleteEvent(event)
    }

    suspend fun getAllEvents(): List<Event> {
        return eventDao.getAllEvents()
    }
}