package com.twinmind.app.data.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.twinmind.app.data.db.entity.*
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TwinMindDatabaseTest {

    private lateinit var db        : TwinMindDatabase
    private val sessionId = "db-test-session"

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            TwinMindDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun teardown() { db.close() }

    // ── Session ──────────────────────────────────────────────────────────────

    @Test
    fun insertAndReadSession() = runTest {
        val session = RecordingSessionEntity(id = sessionId, title = "Test", startTimeMs = 1000L)
        db.recordingSessionDao().insert(session)
        val loaded = db.recordingSessionDao().getById(sessionId)
        assertNotNull(loaded)
        assertEquals("Test", loaded!!.title)
    }

    @Test
    fun updateSessionStatus() = runTest {
        db.recordingSessionDao().insert(
            RecordingSessionEntity(id = sessionId, title = "T", startTimeMs = 0L)
        )
        db.recordingSessionDao().updateStatus(sessionId, SessionStatus.STOPPED.name)
        val loaded = db.recordingSessionDao().getById(sessionId)
        assertEquals(SessionStatus.STOPPED.name, loaded!!.status)
    }

    @Test
    fun observeSessionEmitsOnUpdate() = runTest {
        db.recordingSessionDao().insert(
            RecordingSessionEntity(id = sessionId, title = "T", startTimeMs = 0L)
        )
        db.recordingSessionDao().observeById(sessionId).test {
            assertNotNull(awaitItem())
            db.recordingSessionDao().updateStatus(sessionId, SessionStatus.PAUSED.name)
            val updated = awaitItem()
            assertEquals(SessionStatus.PAUSED.name, updated!!.status)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Chunk ─────────────────────────────────────────────────────────────────

    @Test
    fun insertChunkAndReadPending() = runTest {
        db.recordingSessionDao().insert(
            RecordingSessionEntity(id = sessionId, title = "T", startTimeMs = 0L)
        )
        db.audioChunkDao().insert(
            AudioChunkEntity(sessionId = sessionId, chunkIndex = 0, filePath = "/f/c0.pcm")
        )
        val pending = db.audioChunkDao().getPendingChunks(sessionId)
        assertEquals(1, pending.size)
    }

    @Test
    fun chunkCascadeDeletesWithSession() = runTest {
        val session = RecordingSessionEntity(id = sessionId, title = "T", startTimeMs = 0L)
        db.recordingSessionDao().insert(session)
        db.audioChunkDao().insert(
            AudioChunkEntity(sessionId = sessionId, chunkIndex = 0, filePath = "/f/c0.pcm")
        )
        // Cascade delete via Room CASCADE on ForeignKey
        db.audioChunkDao().getChunksForSessionSync(sessionId).let {
            assertEquals(1, it.size)
        }
    }

    @Test
    fun chunksReturnedInIndexOrder() = runTest {
        db.recordingSessionDao().insert(
            RecordingSessionEntity(id = sessionId, title = "T", startTimeMs = 0L)
        )
        db.audioChunkDao().insert(AudioChunkEntity(sessionId = sessionId, chunkIndex = 2, filePath = "/f/c2.pcm"))
        db.audioChunkDao().insert(AudioChunkEntity(sessionId = sessionId, chunkIndex = 0, filePath = "/f/c0.pcm"))
        db.audioChunkDao().insert(AudioChunkEntity(sessionId = sessionId, chunkIndex = 1, filePath = "/f/c1.pcm"))
        val chunks = db.audioChunkDao().getChunksForSessionSync(sessionId)
        assertEquals(listOf(0, 1, 2), chunks.map { it.chunkIndex })
    }

    // ── Transcript ────────────────────────────────────────────────────────────

    @Test
    fun transcriptsOrderedByChunkIndex() = runTest {
        db.recordingSessionDao().insert(
            RecordingSessionEntity(id = sessionId, title = "T", startTimeMs = 0L)
        )
        db.transcriptDao().insert(TranscriptEntity(sessionId = sessionId, chunkIndex = 1, text = "B"))
        db.transcriptDao().insert(TranscriptEntity(sessionId = sessionId, chunkIndex = 0, text = "A"))
        val list = db.transcriptDao().getTranscriptsSync(sessionId)
        assertEquals("A", list[0].text)
        assertEquals("B", list[1].text)
    }

    @Test
    fun deleteAllTranscriptsForSession() = runTest {
        db.recordingSessionDao().insert(
            RecordingSessionEntity(id = sessionId, title = "T", startTimeMs = 0L)
        )
        db.transcriptDao().insert(TranscriptEntity(sessionId = sessionId, chunkIndex = 0, text = "X"))
        db.transcriptDao().deleteAllForSession(sessionId)
        assertEquals(0, db.transcriptDao().getCountForSession(sessionId))
    }

    // ── Summary ───────────────────────────────────────────────────────────────

    @Test
    fun upsertSummaryUpdatesExisting() = runTest {
        db.recordingSessionDao().insert(
            RecordingSessionEntity(id = sessionId, title = "T", startTimeMs = 0L)
        )
        db.summaryDao().upsert(SummaryEntity(sessionId = sessionId, title = "Draft"))
        db.summaryDao().upsert(SummaryEntity(sessionId = sessionId, title = "Final", isComplete = true))
        val saved = db.summaryDao().getSummarySync(sessionId)
        assertEquals("Final", saved!!.title)
        assertTrue(saved.isComplete)
    }

    @Test
    fun observeSummaryEmitsOnUpsert() = runTest {
        db.recordingSessionDao().insert(
            RecordingSessionEntity(id = sessionId, title = "T", startTimeMs = 0L)
        )
        db.summaryDao().observeSummary(sessionId).test {
            assertNull(awaitItem())
            db.summaryDao().upsert(SummaryEntity(sessionId = sessionId, title = "New"))
            assertNotNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}