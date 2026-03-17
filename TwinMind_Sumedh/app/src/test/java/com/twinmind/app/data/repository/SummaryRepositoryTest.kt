package com.twinmind.app.data.repository

import app.cash.turbine.test
import com.twinmind.app.fake.FakeSummaryDao
import io.mockk.*
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SummaryRepositoryTest {

    private lateinit var repo      : SummaryRepository
    private lateinit var summaryDao: FakeSummaryDao
    private lateinit var summaryApi: com.twinmind.app.network.api.SummaryApi

    private val sessionId = "summary-session-001"

    @Before
    fun setup() {
        summaryDao = FakeSummaryDao()
        summaryApi = mockk()
        repo       = SummaryRepository(summaryDao, summaryApi)
    }

    @Test
    fun `observeSummary emits null when no summary exists`() = runTest {
        repo.observeSummary(sessionId).test {
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getSummaryIfExists returns null when absent`() = runTest {
        assertNull(repo.getSummaryIfExists(sessionId))
    }

    @Test
    fun `getSummaryIfExists returns entity when present`() = runTest {
        summaryDao.upsert(
            com.twinmind.app.data.db.entity.SummaryEntity(
                sessionId = sessionId, isComplete = true
            )
        )
        assertNotNull(repo.getSummaryIfExists(sessionId))
    }

    @Test
    fun `generateSummaryStream emits Loading then Complete in mock mode`() = runTest {
        // We test the mock path directly via BuildConfig — in unit tests
        // TRANSCRIPTION_MODE is always "mock", so we just verify the flow shape
        repo.generateSummaryStream(sessionId, "Some transcript text").test {
            val first = awaitItem()
            assertTrue(first is StreamState.Loading)
            // Collect remaining until terminal
            val events = mutableListOf<StreamState>()
            var item = awaitItem()
            while (item !is StreamState.Complete) {
                events.add(item)
                item = awaitItem()
            }
            events.add(item)
            assertTrue(events.last() is StreamState.Complete)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `generateSummaryStream persists complete summary to dao`() = runTest {
        repo.generateSummaryStream(sessionId, "Test transcript").collect { }
        val saved = summaryDao.getSummarySync(sessionId)
        assertNotNull(saved)
        assertTrue(saved!!.isComplete)
    }
}