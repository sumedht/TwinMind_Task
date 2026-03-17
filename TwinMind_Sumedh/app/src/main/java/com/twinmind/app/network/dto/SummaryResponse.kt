package com.twinmind.app.network.dto

import com.google.gson.annotations.SerializedName

// Streaming SSE delta from OpenAI-style endpoint
data class SummaryStreamChunk(
    @SerializedName("choices") val choices: List<StreamChoice> = emptyList()
) {
    fun deltaText(): String = choices.firstOrNull()?.delta?.content.orEmpty()
    fun isFinished(): Boolean = choices.firstOrNull()?.finishReason != null
}

data class StreamChoice(
    @SerializedName("delta") val delta: StreamDelta,
    @SerializedName("finish_reason") val finishReason: String? = null
)

data class StreamDelta(
    @SerializedName("content") val content: String? = null
)

// Structured summary parsed from full LLM response
data class ParsedSummary(
    val title: String,
    val summary: String,
    val actionItems: List<String>,
    val keyPoints: List<String>
)