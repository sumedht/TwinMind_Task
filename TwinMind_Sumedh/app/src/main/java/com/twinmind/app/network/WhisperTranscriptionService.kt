package com.twinmind.app.network

import com.twinmind.app.network.api.WhisperApi
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

class WhisperTranscriptionService @Inject constructor(
    private val whisperApi: WhisperApi
) : TranscriptionService {
    override suspend fun transcribe(filePath: String, chunkIndex: Int): String {
        val file        = File(filePath)
        val requestFile = file.asRequestBody("audio/pcm".toMediaType())
        val filePart    = MultipartBody.Part.createFormData("file", file.name, requestFile)
        val modelPart   = "whisper-1".toRequestBody("text/plain".toMediaType())
        val formatPart  = "text".toRequestBody("text/plain".toMediaType())
        return whisperApi.transcribe(filePart, modelPart, formatPart).text
    }
}