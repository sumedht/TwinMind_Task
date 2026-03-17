package com.twinmind.app.fake

import com.twinmind.app.data.db.dao.RecordingSessionDao
import com.twinmind.app.data.db.entity.RecordingSessionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeRecordingSessionDao : RecordingSessionDao {
    private val sessions = mutableListOf<RecordingSessionEntity>()
    private val flow     = MutableStateFlow<List<RecordingSessionEntity>>(emptyList())

    override suspend fun insert(session: RecordingSessionEntity) {
        sessions.add(session); flow.value = sessions.toList()
    }

    override suspend fun update(session: RecordingSessionEntity) {
        val idx = sessions.indexOfFirst { it.id == session.id }
        if (idx >= 0) { sessions[idx] = session; flow.value = sessions.toList() }
    }

    override fun getAllSessions(): Flow<List<RecordingSessionEntity>> =
        flow.map { it.sortedByDescending { s -> s.startTimeMs } }

    override suspend fun getById(id: String) = sessions.firstOrNull { it.id == id }

    override fun observeById(id: String): Flow<RecordingSessionEntity?> =
        flow.map { it.firstOrNull { s -> s.id == id } }

    override suspend fun getActiveSession() =
        sessions.firstOrNull { it.status == "RECORDING" || it.status == "PAUSED" }

    override suspend fun updateStatus(id: String, status: String) {
        val idx = sessions.indexOfFirst { it.id == id }
        if (idx >= 0) { sessions[idx] = sessions[idx].copy(status = status); flow.value = sessions.toList() }
    }

    override suspend fun updateDuration(id: String, duration: Long, endTime: Long) {
        val idx = sessions.indexOfFirst { it.id == id }
        if (idx >= 0) { sessions[idx] = sessions[idx].copy(durationMs = duration, endTimeMs = endTime); flow.value = sessions.toList() }
    }

    override suspend fun updateChunkProgress(id: String, total: Int, transcribed: Int) {
        val idx = sessions.indexOfFirst { it.id == id }
        if (idx >= 0) { sessions[idx] = sessions[idx].copy(totalChunks = total, transcribedChunks = transcribed); flow.value = sessions.toList() }
    }
}