package com.twinmind.app.network.api

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Streaming

interface SummaryApi {
    // Streaming endpoint — returns raw ResponseBody so we can read SSE line by line
    @Streaming
    @POST("v1/chat/completions")
    suspend fun summarizeStream(
        @Body body: RequestBody
    ): Response<ResponseBody>
}