package com.twinmind.app.data.repository

import android.util.Log
import com.twinmind.app.data.db.dao.AudioChunkDao
import com.twinmind.app.data.db.dao.RecordingSessionDao
import com.twinmind.app.data.db.dao.TranscriptDao
import com.twinmind.app.data.db.entity.ChunkStatus
import com.twinmind.app.data.db.entity.TranscriptEntity
import com.twinmind.app.network.TranscriptionService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TranscriptRepository @Inject constructor(
    private val transcriptDao: TranscriptDao,
    private val chunkDao: AudioChunkDao,
    private val sessionDao: RecordingSessionDao,
    private val transcriptionService: TranscriptionService
) {
    companion object {
        private const val TAG = "TranscriptRepo"
        private const val MAX_RETRIES = 3
    }

    fun observeTranscripts(sessionId: String): Flow<List<TranscriptEntity>> =
        transcriptDao.getTranscriptsForSession(sessionId)

    /** Full ordered transcript as a single string */
    suspend fun getFullTranscript(sessionId: String): String =
        transcriptDao.getTranscriptsSync(sessionId)
            .sortedBy { it.chunkIndex }
            .joinToString(" ") { it.text.trim() }

    /**
     * Transcribe one chunk — called from TranscriptWorker.
     * On any failure: increments retry count; if retries exhausted resets ALL
     * chunks to PENDING so the whole session is retried (per spec).
     */
    suspend fun transcribeChunk(sessionId: String, chunkId: Int): Result<Unit> {
        val chunk = chunkDao.getPendingChunks(sessionId)
            .firstOrNull { it.id == chunkId }
            ?: return Result.success(Unit)   // already done

        return try {
            chunkDao.updateStatus(chunkId, ChunkStatus.UPLOADING.name)

            val text = transcriptionService.transcribe(chunk.filePath, chunk.chunkIndex)

            transcriptDao.insert(TranscriptEntity(
                sessionId = sessionId,
                chunkIndex = chunk.chunkIndex,
                text = text
            ))
            chunkDao.updateStatus(chunkId, ChunkStatus.DONE.name)

            // Update progress counter on session
            val done  = chunkDao.getChunksForSessionSync(sessionId).count { it.status == ChunkStatus.DONE.name }
            val total = chunkDao.getChunkCount(sessionId)
            sessionDao.updateChunkProgress(sessionId, total, done)

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Chunk $chunkId transcription failed", e)
            chunkDao.incrementRetry(chunkId)
            val updated = chunkDao.getPendingChunks(sessionId).firstOrNull { it.id == chunkId }
            if ((updated?.retryCount ?: 0) >= MAX_RETRIES) {
                Log.w(TAG, "Max retries hit for chunk $chunkId — resetting ALL chunks for retry")
                resetAllChunks(sessionId)
            } else {
                chunkDao.updateStatus(chunkId, ChunkStatus.PENDING.name)
            }
            Result.failure(e)
        }
    }

    private suspend fun resetAllChunks(sessionId: String) {
        transcriptDao.deleteAllForSession(sessionId)
        chunkDao.getChunksForSessionSync(sessionId).forEach {
            chunkDao.updateStatus(it.id, ChunkStatus.PENDING.name)
        }
    }

    suspend fun transcribeAllPendingChunks(sessionId: String): Result<Unit> {
        val pending = chunkDao.getPendingChunks(sessionId)
        var anyFailed = false
        for (chunk in pending) {
            val r = transcribeChunk(sessionId, chunk.id)
            if (r.isFailure) anyFailed = true
        }
        return if (anyFailed) Result.failure(Exception("Some chunks failed")) else Result.success(Unit)
    }
}