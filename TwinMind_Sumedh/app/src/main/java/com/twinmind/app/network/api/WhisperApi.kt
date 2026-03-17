package com.twinmind.app.network.api

import com.twinmind.app.network.dto.WhisperTranscriptResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface WhisperApi {
    @Multipart
    @POST("v1/audio/transcriptions")
    suspend fun transcribe(
        @Part file: MultipartBody.Part,
        @Part("model") model: RequestBody,
        @Part("response_format") format: RequestBody
    ): WhisperTranscriptResponse
}