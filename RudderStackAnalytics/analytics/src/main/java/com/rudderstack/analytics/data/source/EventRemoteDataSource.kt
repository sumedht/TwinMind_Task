package com.rudderstack.analytics.data.source

import com.rudderstack.analytics.data.model.Event
import com.rudderstack.analytics.infrastrcture.NetworkClient
import okhttp3.Response

internal class EventRemoteDataSource(
    private val networkClient: NetworkClient
) {
    fun sendEvent(event: Event): Response {
        return networkClient.sendEvent(event)
    }
}