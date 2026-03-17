package com.twinmind.app.network

import kotlinx.coroutines.delay
import javax.inject.Inject

class MockTranscriptionService @Inject constructor() : TranscriptionService {
    private val sentences = listOf(
        "Good morning everyone, let's get started with today's meeting.",
        "We need to review the quarterly targets before end of week.",
        "The design team shared their latest mockups yesterday.",
        "Action item: follow up with the client by Thursday.",
        "Key point: budget approval is needed before proceeding.",
        "Let's schedule a follow-up call for next Monday.",
        "The infrastructure migration is on track for Q2.",
        "Please review the attached document before our next sync."
    )

    override suspend fun transcribe(filePath: String, chunkIndex: Int): String {
        delay(800) // simulate network latency
        return sentences[chunkIndex % sentences.size]
    }
}