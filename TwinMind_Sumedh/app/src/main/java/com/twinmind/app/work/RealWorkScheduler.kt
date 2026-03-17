package com.twinmind.app.work

import android.content.Context
import com.twinmind.app.summary.worker.SummaryWorker
import com.twinmind.app.transcript.worker.TranscriptWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class RealWorkScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) : WorkScheduler {

    override fun enqueueTranscript(sessionId: String) =
        TranscriptWorker.enqueue(context, sessionId)

    override fun enqueueChunk(sessionId: String, chunkId: Int) =
        TranscriptWorker.enqueueChunk(context, sessionId, chunkId)

    override fun enqueueSummary(sessionId: String) =
        SummaryWorker.enqueue(context, sessionId)
}