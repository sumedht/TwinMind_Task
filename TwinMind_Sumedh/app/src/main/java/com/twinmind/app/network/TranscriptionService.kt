package com.twinmind.app.network

/**
 * Pluggable transcription backend.
 * Switch via BuildConfig.TRANSCRIPTION_MODE = "mock" | "whisper" | "gemini"
 */
interface TranscriptionService {
    suspend fun transcribe(filePath: String, chunkIndex: Int): String
}