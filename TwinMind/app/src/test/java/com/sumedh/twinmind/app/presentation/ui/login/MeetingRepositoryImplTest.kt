package com.sumedh.twinmind.app.presentation.ui.login

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.sumedh.twinmind.app.data.db.MeetingDao
import com.sumedh.twinmind.app.data.db.TranscriptDao
import com.sumedh.twinmind.app.data.model.MeetingEntity
import com.sumedh.twinmind.app.data.repository.MeetingRepositoryImpl
import com.sumedh.twinmind.app.domain.repository.MeetingRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.OffsetDateTime

class MeetingRepositoryImplTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var repository: MeetingRepository
    private val mockMeetingDao: MeetingDao = mock()
    private val mockTranscriptDao: TranscriptDao = mock()

    @Before
    fun setUp() {
        repository = MeetingRepositoryImpl(mockMeetingDao, mockTranscriptDao)
    }

    @Test
    fun `startMeeting inserts a meeting into the dao`(): Unit = runBlocking {
        // Arrange
        val meetingTitle = "New Project Kick-off"

        // Act
        repository.startMeeting(meetingTitle)

        // Assert
        // Verify that the insertMeeting function on the DAO was called.
        // We can't easily check the exact object due to the timestamp,
        // so we verify the interaction itself.
        verify(mockMeetingDao).insertMeeting(any())
    }

    @Test
    fun `getAllMeetings returns mapped meetings from dao`() = runBlocking {
        // Arrange
        val meetingEntity = MeetingEntity(id = 1, title = "Test Meeting", createdAt = OffsetDateTime.now())
        whenever(mockMeetingDao.getAllMeetings()).thenReturn(flowOf(listOf(meetingEntity)))

        // Act
        val meetings = repository.getAllMeetings().first()

        // Assert
        assertThat(meetings).hasSize(1)
        assertThat(meetings[0].id).isEqualTo(meetingEntity.id)
        assertThat(meetings[0].title).isEqualTo(meetingEntity.title)
    }
}