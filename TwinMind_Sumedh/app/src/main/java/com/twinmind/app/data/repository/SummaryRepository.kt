package com.twinmind.app.data.repository

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.twinmind.app.BuildConfig
import com.twinmind.app.data.db.dao.SummaryDao
import com.twinmind.app.data.db.entity.SummaryEntity
import com.twinmind.app.network.api.SummaryApi
import com.twinmind.app.network.dto.ParsedSummary
import com.twinmind.app.network.dto.SummaryStreamChunk
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class SummaryRepository @Inject constructor(
    private val summaryDao: SummaryDao,
    private val summaryApi: SummaryApi
) {
    companion object {
        private const val TAG = "SummaryRepo"
    }

    private val gson = Gson()

    fun observeSummary(sessionId: String): Flow<SummaryEntity?> =
        summaryDao.observeSummary(sessionId)

    /**
     * Streams partial summary text back as it arrives from the LLM.
     * Persists each delta to Room so the UI stays in sync even if app is killed.
     * Emits [StreamState] sealed class for the UI to render.
     */
    fun generateSummaryStream(sessionId: String, fullTranscript: String): Flow<StreamState> = flow {
        emit(StreamState.Loading)

        if (BuildConfig.TRANSCRIPTION_MODE == "mock") {
            val mock = ParsedSummary(
                title       = "Team Sync — ${java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault()).format(java.util.Date())}",
                summary     = "The team discussed quarterly targets, design updates, and infrastructure migration. Budget approval was identified as a blocker.",
                actionItems = listOf("Follow up with client by Thursday", "Schedule follow-up call for Monday", "Review attached document"),
                keyPoints   = listOf("Budget approval needed before proceeding", "Infrastructure migration on track for Q2", "Design mockups shared")
            )
            // Stream it word-by-word to simulate SSE
            val words = mock.summary.split(" ")
            for (word in words) {
                delay(60)
                summaryDao.upsert(SummaryEntity(
                    sessionId = sessionId,
                    title = mock.title,
                    summary = words.take(words.indexOf(word) + 1).joinToString(" "),
                    isComplete = false
                ))
            }
            summaryDao.upsert(SummaryEntity(
                sessionId    = sessionId,
                title        = mock.title,
                summary      = mock.summary,
                actionItems  = Gson().toJson(mock.actionItems),
                keyPoints    = Gson().toJson(mock.keyPoints),
                isComplete   = true
            ))
            emit(StreamState.Complete(mock))
            return@flow
        }

        // Ensure a placeholder row exists so UI can observe immediately
        summaryDao.upsert(SummaryEntity(sessionId = sessionId, isComplete = false))

        try {
            val systemPrompt = """
                You are a meeting assistant. Given a transcript, return a JSON object with exactly:
                {
                  "title": "string",
                  "summary": "string (2-4 sentences)",
                  "action_items": ["item1", "item2"],
                  "key_points": ["point1", "point2"]
                }
                Return ONLY valid JSON. No markdown fences.
            """.trimIndent()

            val body = buildRequestBody(systemPrompt, fullTranscript, stream = true)
            val response = summaryApi.summarizeStream(body)

            if (!response.isSuccessful) {
                val errMsg = "HTTP ${response.code()}: ${response.errorBody()?.string()}"
                emit(StreamState.Error(errMsg))
                return@flow
            }

            val responseBody = response.body() ?: run {
                emit(StreamState.Error("Empty response body"))
                return@flow
            }

            val buffer = StringBuilder()
            responseBody.byteStream().bufferedReader().use { reader ->
                reader.forEachLine { line ->
                    if (line.startsWith("data: ") && line != "data: [DONE]") {
                        val json = line.removePrefix("data: ")
                        runCatching {
                            gson.fromJson(json, SummaryStreamChunk::class.java)
                                .deltaText()
                        }.getOrNull()?.let { delta ->
                            if (delta.isNotEmpty()) {
                                buffer.append(delta)
                                // Persist partial to DB
                                summaryDao.upsert(
                                    SummaryEntity(
                                        sessionId = sessionId,
                                        title = "Generating...",
                                        summary = buffer.toString(),
                                        isComplete = false
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Parse the complete JSON
            val parsed = parseStructuredSummary(buffer.toString())
            val entity = SummaryEntity(
                sessionId    = sessionId,
                title        = parsed.title,
                summary      = parsed.summary,
                actionItems  = gson.toJson(parsed.actionItems),
                keyPoints    = gson.toJson(parsed.keyPoints),
                isComplete   = true
            )
            summaryDao.upsert(entity)
            emit(StreamState.Complete(parsed))

        } catch (e: Exception) {
            Log.e(TAG, "Summary generation failed", e)
            emit(StreamState.Error(e.message ?: "Unknown error"))
        }
    }

    /** Resumes summary generation from Room if already partially complete */
    suspend fun getSummaryIfExists(sessionId: String): SummaryEntity? =
        summaryDao.getSummarySync(sessionId)

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private fun buildRequestBody(system: String, transcript: String, stream: Boolean) =
        JsonObject().apply {
            addProperty("model", "gpt-4o-mini")
            addProperty("stream", stream)
            add("messages", com.google.gson.JsonArray().apply {
                add(JsonObject().apply {
                    addProperty("role", "system")
                    addProperty("content", system)
                })
                add(JsonObject().apply {
                    addProperty("role", "user")
                    addProperty("content", transcript.take(12_000)) // token safety
                })
            })
        }.toString().toRequestBody("application/json".toMediaType())

    private fun parseStructuredSummary(raw: String): ParsedSummary {
        return try {
            val obj = gson.fromJson(raw.trim(), JsonObject::class.java)
            ParsedSummary(
                title       = obj.get("title")?.asString ?: "Untitled Meeting",
                summary     = obj.get("summary")?.asString ?: raw,
                actionItems = obj.getAsJsonArray("action_items")
                    ?.map { it.asString } ?: emptyList(),
                keyPoints   = obj.getAsJsonArray("key_points")
                    ?.map { it.asString } ?: emptyList()
            )
        } catch (e: Exception) {
            Log.w(TAG, "JSON parse failed, using raw text as summary")
            ParsedSummary(
                title       = "Meeting Summary",
                summary     = raw,
                actionItems = emptyList(),
                keyPoints   = emptyList()
            )
        }
    }
}

sealed class StreamState {
    data object Loading : StreamState()
    data class Complete(val summary: ParsedSummary) : StreamState()
    data class Error(val message: String) : StreamState()
}