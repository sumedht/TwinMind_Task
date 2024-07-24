package com.rudderstack.analytics.data
import android.content.Context
import com.rudderstack.analytics.data.model.Event
import com.rudderstack.analytics.data.source.EventLocalDataSource
import com.rudderstack.analytics.data.source.EventRemoteDataSource
import com.rudderstack.analytics.domain.model.EventDomain
import com.rudderstack.analytics.infrastrcture.NetworkClient
import com.rudderstack.analytics.infrastrcture.NetworkUtils

internal class EventRepository(private val context: Context) {
    private val localDataSource = EventLocalDataSource(context)
    private val remoteDataSource = EventRemoteDataSource(NetworkClient())
    private val eventQueue = EventQueue()
    private var isQueueEmpty = false

    suspend fun sendEvent(eventDomain: EventDomain) {
        val event = Event(
            name = eventDomain.name,
            properties = eventDomain.properties
        )
            localDataSource.saveEvent(event)
            eventQueue.addEvent(event)
            if (NetworkUtils.isNetworkAvailable(context)) {
                processEvents()
            }
    }

    private suspend fun processEvents() {
        while (isQueueEmpty) {
            val event = eventQueue.getEvent()
            if (event != null) {
                val response = remoteDataSource.sendEvent(event)
                when(response.code) {
                    200 ->  localDataSource.deleteEvent(event)
                    400 -> {
                        println("Bad request: ${response.message}")
                        localDataSource.deleteEvent(event)
                    }
                    500 -> {
                        println("Server error: ${response.message}")
                    }
                    else -> {
                        println("Unexpected error: ${response.message}")
                    }
                }
            } else {
                isQueueEmpty = true
            }
        }
    }

    suspend fun retryOfflineEvents() {
        val events = localDataSource.getAllEvents()
        if (events.isEmpty()) {
            return
        }
        for (event in events) {
            eventQueue.addEvent(event)
        }
        processEvents()
    }
}