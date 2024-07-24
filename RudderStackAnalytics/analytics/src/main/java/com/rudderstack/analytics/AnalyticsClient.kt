package com.rudderstack.analytics

import android.content.Context
import com.rudderstack.analytics.domain.model.EventDomain
import com.rudderstack.analytics.domain.usecase.SendEventUseCase
import com.rudderstack.analytics.infrastrcture.NetworkUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

class AnalyticsClient private constructor(
    context: Context
) {
    private val dispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val sendEventUseCase = SendEventUseCase(context)

    init {
        retryOfflineEvents(context)
    }

    companion object {
        @Volatile
        private var analyticsClient: AnalyticsClient? = null

        fun getInstance(context: Context): AnalyticsClient {
            return analyticsClient ?: synchronized(this) {
                analyticsClient ?: AnalyticsClient(context).also { analyticsClient = it }
            }
        }
    }

    fun sendEvent(eventName: String, properties: Map<String, Any>) {
        val event = EventDomain(eventName, properties)
        CoroutineScope(dispatcher).launch {
            sendEventUseCase.execute(event)
        }
    }

    private fun retryOfflineEvents(context: Context) {
        CoroutineScope(dispatcher).launch {
            if (NetworkUtils.isNetworkAvailable(context)) {
                sendEventUseCase.retryOfflineEvents()
            }
        }
    }
}