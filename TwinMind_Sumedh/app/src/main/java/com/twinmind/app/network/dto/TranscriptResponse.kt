package com.twinmind.app.network.dto

import com.google.gson.annotations.SerializedName

data class WhisperTranscriptResponse(
    @SerializedName("text") val text: String
)

data class GeminiTranscriptResponse(
    @SerializedName("candidates") val candidates: List<GeminiCandidate>
) {
    fun extractText(): String =
        candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text.orEmpty()
}

data class GeminiCandidate(
    @SerializedName("content") val content: GeminiContent
)

data class GeminiContent(
    @SerializedName("parts") val parts: List<GeminiPart>
)

data class GeminiPart(
    @SerializedName("text") val text: String
)