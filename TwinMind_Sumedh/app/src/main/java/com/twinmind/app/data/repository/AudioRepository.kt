package com.twinmind.app.data.repository

import com.twinmind.app.data.db.dao.AudioChunkDao
import com.twinmind.app.data.db.dao.RecordingSessionDao
import com.twinmind.app.data.db.entity.AudioChunkEntity
import com.twinmind.app.data.db.entity.RecordingSessionEntity
import com.twinmind.app.data.db.entity.SessionStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioRepository @Inject constructor(
    private val sessionDao: RecordingSessionDao,
    private val chunkDao: AudioChunkDao
) {
    fun observeAllSessions(): Flow<List<RecordingSessionEntity>> =
        sessionDao.getAllSessions()

    fun observeSession(id: String): Flow<RecordingSessionEntity?> =
        sessionDao.observeById(id)

    suspend fun getSession(id: String): RecordingSessionEntity? =
        sessionDao.getById(id)

    suspend fun getActiveSession(): RecordingSessionEntity? =
        sessionDao.getActiveSession()

    fun observeChunks(sessionId: String): Flow<List<AudioChunkEntity>> =
        chunkDao.getChunksForSession(sessionId)

    suspend fun getPendingChunks(sessionId: String): List<AudioChunkEntity> =
        chunkDao.getPendingChunks(sessionId)

    suspend fun updateChunkStatus(chunkId: Int, status: String) =
        chunkDao.updateStatus(chunkId, status)

    suspend fun incrementChunkRetry(chunkId: Int) =
        chunkDao.incrementRetry(chunkId)

    suspend fun markSessionStopped(sessionId: String) =
        sessionDao.updateStatus(sessionId, SessionStatus.STOPPED.name)
}