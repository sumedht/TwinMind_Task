package com.twinmind.app.fake

import com.twinmind.app.data.db.dao.AudioChunkDao
import com.twinmind.app.data.db.entity.AudioChunkEntity
import com.twinmind.app.data.db.entity.ChunkStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeAudioChunkDao : AudioChunkDao {
    private val chunks = mutableListOf<AudioChunkEntity>()
    private val flow   = MutableStateFlow<List<AudioChunkEntity>>(emptyList())

    override suspend fun insert(chunk: AudioChunkEntity): Long {
        val id      = (chunks.maxOfOrNull { it.id } ?: 0) + 1
        val entity  = chunk.copy(id = id)
        chunks.add(entity)
        flow.value  = chunks.toList()
        return id.toLong()
    }

    override suspend fun update(chunk: AudioChunkEntity) {
        val idx = chunks.indexOfFirst { it.id == chunk.id }
        if (idx >= 0) { chunks[idx] = chunk; flow.value = chunks.toList() }
    }

    override fun getChunksForSession(sessionId: String): Flow<List<AudioChunkEntity>> =
        flow.map { it.filter { c -> c.sessionId == sessionId } }

    override suspend fun getChunksForSessionSync(sessionId: String) =
        chunks.filter { it.sessionId == sessionId }

    override suspend fun getPendingChunks(sessionId: String) =
        chunks.filter { it.sessionId == sessionId && it.status != ChunkStatus.DONE.name }

    override suspend fun updateStatus(id: Int, status: String) {
        val idx = chunks.indexOfFirst { it.id == id }
        if (idx >= 0) { chunks[idx] = chunks[idx].copy(status = status); flow.value = chunks.toList() }
    }

    override suspend fun incrementRetry(id: Int) {
        val idx = chunks.indexOfFirst { it.id == id }
        if (idx >= 0) { chunks[idx] = chunks[idx].copy(retryCount = chunks[idx].retryCount + 1); flow.value = chunks.toList() }
    }

    override suspend fun getChunkCount(sessionId: String) =
        chunks.count { it.sessionId == sessionId }

    override suspend fun deleteById(id: Int) {
        chunks.removeAll { it.id == id }
        flow.value = chunks.toList()
    }
}