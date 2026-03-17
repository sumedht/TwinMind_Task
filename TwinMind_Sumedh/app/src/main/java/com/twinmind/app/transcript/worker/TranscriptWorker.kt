package com.twinmind.app.transcript.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.twinmind.app.data.db.dao.AudioChunkDao
import com.twinmind.app.data.db.dao.RecordingSessionDao
import com.twinmind.app.data.db.entity.ChunkStatus
import com.twinmind.app.data.repository.TranscriptRepository
import com.twinmind.app.summary.worker.SummaryWorker
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit

@HiltWorker
class TranscriptWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams       : WorkerParameters,
    private val transcriptRepository: TranscriptRepository,
    private val chunkDao            : AudioChunkDao,
    private val sessionDao          : RecordingSessionDao
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val KEY_SESSION_ID = "session_id"
        const val KEY_CHUNK_ID   = "chunk_id"
        private const val TAG_PREFIX = "transcript_"

        fun enqueueChunk(context: Context, sessionId: String, chunkId: Int) {
            val request = OneTimeWorkRequestBuilder<TranscriptWorker>()
                .setInputData(
                    workDataOf(
                        KEY_SESSION_ID to sessionId,
                        KEY_CHUNK_ID   to chunkId
                    )
                )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.SECONDS)
                .addTag("$TAG_PREFIX$sessionId")
                .build()
            WorkManager.getInstance(context).enqueue(request)
        }

        fun enqueue(context: Context, sessionId: String) {
            val request = OneTimeWorkRequestBuilder<TranscriptWorker>()
                .setInputData(
                    workDataOf(
                        KEY_SESSION_ID to sessionId,
                        KEY_CHUNK_ID   to -1
                    )
                )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.SECONDS)
                .addTag("$TAG_PREFIX$sessionId")
                .build()
            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    "transcript_session_$sessionId",
                    ExistingWorkPolicy.KEEP,
                    request
                )
        }
    }

    override suspend fun doWork(): Result {
        val sessionId = inputData.getString(KEY_SESSION_ID)
            ?: return Result.failure()
        val chunkId   = inputData.getInt(KEY_CHUNK_ID, -1)

        val transcriptResult = if (chunkId == -1) {
            transcriptRepository.transcribeAllPendingChunks(sessionId)
        } else {
            transcriptRepository.transcribeChunk(sessionId, chunkId)
        }

        if (transcriptResult.isFailure) return Result.retry()

        checkAndTriggerSummary(sessionId)

        return Result.success()
    }

    private fun checkAndTriggerSummary(sessionId: String) {
        val allChunks = runBlocking {
            chunkDao.getChunksForSessionSync(sessionId)
        }
        if (allChunks.isEmpty()) return

        val allDone = allChunks.all { it.status == ChunkStatus.DONE.name }
        if (allDone) {
            SummaryWorker.enqueue(applicationContext, sessionId)
        }
    }
}