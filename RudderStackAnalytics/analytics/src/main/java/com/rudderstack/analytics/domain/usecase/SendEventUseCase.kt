package com.rudderstack.analytics.domain.usecase

import android.content.Context
import com.rudderstack.analytics.data.EventRepository
import com.rudderstack.analytics.domain.model.EventDomain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class SendEventUseCase(context: Context) {
    private val eventRepository = EventRepository(context)

    suspend fun execute(event: EventDomain) {
        withContext(Dispatchers.IO) {
            eventRepository.sendEvent(event)
        }
    }

    suspend fun retryOfflineEvents() {
        withContext(Dispatchers.IO) {
            eventRepository.retryOfflineEvents()
        }
    }
}