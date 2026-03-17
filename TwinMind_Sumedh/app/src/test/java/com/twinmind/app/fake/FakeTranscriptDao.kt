package com.twinmind.app.fake

import com.twinmind.app.data.db.dao.TranscriptDao
import com.twinmind.app.data.db.entity.TranscriptEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeTranscriptDao : TranscriptDao {
    private val transcripts = mutableListOf<TranscriptEntity>()
    private val flow        = MutableStateFlow<List<TranscriptEntity>>(emptyList())

    override suspend fun insert(transcript: TranscriptEntity) {
        transcripts.add(transcript)
        flow.value = transcripts.toList()
    }

    override fun getTranscriptsForSession(sessionId: String): Flow<List<TranscriptEntity>> =
        flow.map { it.filter { t -> t.sessionId == sessionId }.sortedBy { t -> t.chunkIndex } }

    override suspend fun getTranscriptsSync(sessionId: String) =
        transcripts.filter { it.sessionId == sessionId }.sortedBy { it.chunkIndex }

    override suspend fun deleteAllForSession(sessionId: String) {
        transcripts.removeAll { it.sessionId == sessionId }
        flow.value = transcripts.toList()
    }

    override suspend fun getCountForSession(sessionId: String) =
        transcripts.count { it.sessionId == sessionId }
}