package com.twinmind.app.data.repository

import app.cash.turbine.test
import com.twinmind.app.data.db.entity.AudioChunkEntity
import com.twinmind.app.data.db.entity.ChunkStatus
import com.twinmind.app.data.db.entity.RecordingSessionEntity
import com.twinmind.app.fake.*
import com.twinmind.app.network.TranscriptionService
import io.mockk.*
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class TranscriptRepositoryTest {

    private lateinit var repo                : TranscriptRepository
    private lateinit var chunkDao            : FakeAudioChunkDao
    private lateinit var transcriptDao       : FakeTranscriptDao
    private lateinit var sessionDao          : FakeRecordingSessionDao
    private lateinit var transcriptionService: TranscriptionService

    private val sessionId = "test-session-001"

    @Before
    fun setup() {
        chunkDao             = FakeAudioChunkDao()
        transcriptDao        = FakeTranscriptDao()
        sessionDao           = FakeRecordingSessionDao()
        transcriptionService = mockk()
        repo = TranscriptRepository(
            transcriptDao        = transcriptDao,
            chunkDao             = chunkDao,
            sessionDao           = sessionDao,
            transcriptionService = transcriptionService
        )
    }

    @Test
    fun `transcribeChunk saves transcript to dao on success`() = runTest {
        seedSession()
        val chunkId = seedChunk(index = 0)
        coEvery { transcriptionService.transcribe(any(), any()) } returns "Hello world"

        val result = repo.transcribeChunk(sessionId, chunkId)

        assertTrue(result.isSuccess)
        val saved = transcriptDao.getTranscriptsSync(sessionId)
        assertEquals(1, saved.size)
        assertEquals("Hello world", saved.first().text)
        assertEquals(0, saved.first().chunkIndex)
    }

    @Test
    fun `transcribeChunk marks chunk as DONE on success`() = runTest {
        seedSession()
        val chunkId = seedChunk(index = 0)
        coEvery { transcriptionService.transcribe(any(), any()) } returns "Done text"

        repo.transcribeChunk(sessionId, chunkId)

        val chunks = chunkDao.getChunksForSessionSync(sessionId)
        assertEquals(ChunkStatus.DONE.name, chunks.first().status)
    }

    @Test
    fun `transcribeChunk retries on failure and increments retry count`() = runTest {
        seedSession()
        val chunkId = seedChunk(index = 0)
        coEvery { transcriptionService.transcribe(any(), any()) } throws RuntimeException("Network error")

        repo.transcribeChunk(sessionId, chunkId)

        val chunk = chunkDao.getChunksForSessionSync(sessionId).first()
        assertEquals(1, chunk.retryCount)
    }

    @Test
    fun `transcribeChunk resets ALL chunks when max retries exceeded`() = runTest {
        seedSession()
        val chunkId = seedChunk(index = 0, retryCount = 3)  // already at max
        val savedTranscript = com.twinmind.app.data.db.entity.TranscriptEntity(
            sessionId = sessionId, chunkIndex = 0, text = "old"
        )
        transcriptDao.insert(savedTranscript)
        coEvery { transcriptionService.transcribe(any(), any()) } throws RuntimeException("fail")

        repo.transcribeChunk(sessionId, chunkId)

        // All transcripts for session should be deleted on reset
        assertEquals(0, transcriptDao.getCountForSession(sessionId))
    }

    @Test
    fun `getFullTranscript returns chunks in correct order`() = runTest {
        seedSession()
        transcriptDao.insert(com.twinmind.app.data.db.entity.TranscriptEntity(sessionId = sessionId, chunkIndex = 2, text = "Third"))
        transcriptDao.insert(com.twinmind.app.data.db.entity.TranscriptEntity(sessionId = sessionId, chunkIndex = 0, text = "First"))
        transcriptDao.insert(com.twinmind.app.data.db.entity.TranscriptEntity(sessionId = sessionId, chunkIndex = 1, text = "Second"))

        val full = repo.getFullTranscript(sessionId)
        assertEquals("First Second Third", full)
    }

    @Test
    fun `observeTranscripts emits updates reactively`() = runTest {
        seedSession()
        repo.observeTranscripts(sessionId).test {
            assertEquals(0, awaitItem().size)
            transcriptDao.insert(
                com.twinmind.app.data.db.entity.TranscriptEntity(
                    sessionId = sessionId, chunkIndex = 0, text = "New chunk"
                )
            )
            val updated = awaitItem()
            assertEquals(1, updated.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `transcribeAllPendingChunks processes all pending in order`() = runTest {
        seedSession()
        val id0 = seedChunk(index = 0)
        val id1 = seedChunk(index = 1)
        coEvery { transcriptionService.transcribe(any(), 0) } returns "Chunk zero"
        coEvery { transcriptionService.transcribe(any(), 1) } returns "Chunk one"

        val result = repo.transcribeAllPendingChunks(sessionId)

        assertTrue(result.isSuccess)
        val transcripts = transcriptDao.getTranscriptsSync(sessionId)
        assertEquals(2, transcripts.size)
        assertEquals("Chunk zero", transcripts[0].text)
        assertEquals("Chunk one",  transcripts[1].text)
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private suspend fun seedSession() {
        sessionDao.insert(RecordingSessionEntity(id = sessionId, title = "Test", startTimeMs = 0L))
    }

    private suspend fun seedChunk(index: Int, retryCount: Int = 0): Int {
        val id = chunkDao.insert(
            AudioChunkEntity(
                sessionId  = sessionId,
                chunkIndex = index,
                filePath   = "/fake/chunk_$index.pcm",
                retryCount = retryCount
            )
        )
        return id.toInt()
    }
}