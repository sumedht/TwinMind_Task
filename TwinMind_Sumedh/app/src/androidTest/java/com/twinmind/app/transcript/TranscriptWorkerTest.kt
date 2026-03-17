package com.twinmind.app.transcript

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker.Result
import androidx.work.testing.*
import androidx.work.workDataOf
import com.twinmind.app.data.repository.TranscriptRepository
import com.twinmind.app.transcript.worker.TranscriptWorker
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TranscriptWorkerTest {

    private lateinit var context    : Context
    private lateinit var mockRepo   : TranscriptRepository

    @Before
    fun setup() {
        context  = ApplicationProvider.getApplicationContext()
        mockRepo = mockk(relaxed = true)
    }

    @Test
    fun worker_returns_success_when_transcription_succeeds() = runTest {
        coEvery { mockRepo.transcribeChunk(any(), any()) } returns kotlin.Result.success(Unit)

        val worker = TestListenableWorkerBuilder<TranscriptWorker>(context)
            .setInputData(workDataOf(
                TranscriptWorker.KEY_SESSION_ID to "session-1",
                TranscriptWorker.KEY_CHUNK_ID   to 1
            ))
            .build()

        // Inject repo via reflection for test
        val field = TranscriptWorker::class.java.getDeclaredField("transcriptRepository")
        field.isAccessible = true
        field.set(worker, mockRepo)

        val result = worker.startWork().get()
        assertEquals(Result.success(), result)
    }

    @Test
    fun worker_returns_retry_when_transcription_fails() = runTest {
        coEvery { mockRepo.transcribeChunk(any(), any()) } returns
                kotlin.Result.failure(Exception("Network error"))

        val worker = TestListenableWorkerBuilder<TranscriptWorker>(context)
            .setInputData(workDataOf(
                TranscriptWorker.KEY_SESSION_ID to "session-1",
                TranscriptWorker.KEY_CHUNK_ID   to 1
            ))
            .build()

        val field = TranscriptWorker::class.java.getDeclaredField("transcriptRepository")
        field.isAccessible = true
        field.set(worker, mockRepo)

        val result = worker.startWork().get()
        assertEquals(Result.retry(), result)
    }

    @Test
    fun worker_returns_failure_when_sessionId_is_missing() = runTest {
        val worker = TestListenableWorkerBuilder<TranscriptWorker>(context)
            .setInputData(workDataOf(TranscriptWorker.KEY_CHUNK_ID to 1))
            .build()

        val field = TranscriptWorker::class.java.getDeclaredField("transcriptRepository")
        field.isAccessible = true
        field.set(worker, mockRepo)

        val result = worker.startWork().get()
        assertEquals(Result.failure(), result)
    }
}