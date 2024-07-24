package com.rudderstack.analytics.infrastrcture

import com.google.gson.Gson
import com.rudderstack.analytics.data.model.Event
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import com.rudderstack.analytics.BuildConfig

class NetworkClient() {
    private val client = OkHttpClient()
    private val gson = Gson()
    private val scope = CoroutineScope(Dispatchers.IO)
    private lateinit var response: Response
    private val endpoint: String = BuildConfig.API_ENDPOINT

    fun sendEvent(event: Event): Response {
        scope.launch {
            val json = gson.toJson(event)
            val body = json.toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(endpoint)
                .post(body)
                .build()

            response = client.newCall(request).execute()
        }
        return response
    }
}