package com.twinmind.app.work

interface WorkScheduler {
    fun enqueueTranscript(sessionId: String)
    fun enqueueChunk(sessionId: String, chunkId: Int)
    fun enqueueSummary(sessionId: String)
}