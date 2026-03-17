package com.twinmind.app.summary.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.twinmind.app.data.repository.StreamState
import com.twinmind.app.data.repository.SummaryRepository
import com.twinmind.app.data.repository.TranscriptRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class SummaryWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams       : WorkerParameters,
    private val transcriptRepository: TranscriptRepository,
    private val summaryRepository   : SummaryRepository
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val KEY_SESSION_ID = "session_id"

        fun enqueue(context: Context, sessionId: String) {
            val request = OneTimeWorkRequestBuilder<SummaryWorker>()
                .setInputData(workDataOf(KEY_SESSION_ID to sessionId))
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build()
            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    "summary_$sessionId",
                    ExistingWorkPolicy.KEEP,
                    request
                )
        }
    }

    override suspend fun doWork(): Result {
        val sessionId = inputData.getString(KEY_SESSION_ID)
            ?: return Result.failure()

        // Skip if already complete
        val existing = summaryRepository.getSummaryIfExists(sessionId)
        if (existing?.isComplete == true) return Result.success()

        // Retry if transcript not ready yet
        val transcript = transcriptRepository.getFullTranscript(sessionId)
        if (transcript.isBlank()) return Result.retry()

        var failed = false
        summaryRepository.generateSummaryStream(sessionId, transcript)
            .collect { state ->
                if (state is StreamState.Error) {
                    android.util.Log.e("SummaryWorker", "Summary failed: ${state.message}")
                    failed = true
                }
            }

        return if (failed) Result.retry() else Result.success()
    }
}