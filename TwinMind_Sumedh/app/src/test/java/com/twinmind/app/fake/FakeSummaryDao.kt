package com.twinmind.app.fake

import com.twinmind.app.data.db.dao.SummaryDao
import com.twinmind.app.data.db.entity.SummaryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeSummaryDao : SummaryDao {
    private val summaries = mutableListOf<SummaryEntity>()
    private val flow      = MutableStateFlow<List<SummaryEntity>>(emptyList())

    override suspend fun insert(summary: SummaryEntity): Long {
        summaries.add(summary); flow.value = summaries.toList(); return 1L
    }

    override suspend fun update(summary: SummaryEntity) {
        val idx = summaries.indexOfFirst { it.sessionId == summary.sessionId }
        if (idx >= 0) { summaries[idx] = summary; flow.value = summaries.toList() }
    }

    override fun observeSummary(sessionId: String): Flow<SummaryEntity?> =
        flow.map { it.firstOrNull { s -> s.sessionId == sessionId } }

    override suspend fun getSummarySync(sessionId: String) =
        summaries.firstOrNull { it.sessionId == sessionId }

    override fun upsert(summary: SummaryEntity) {
        val idx = summaries.indexOfFirst { it.sessionId == summary.sessionId }
        if (idx >= 0) { summaries[idx] = summary } else { summaries.add(summary) }
        flow.value = summaries.toList()
    }
}