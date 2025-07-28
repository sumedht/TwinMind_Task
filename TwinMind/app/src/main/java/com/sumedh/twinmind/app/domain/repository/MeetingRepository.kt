package com.sumedh.twinmind.app.domain.repository

import com.sumedh.twinmind.app.domain.model.Meeting
import com.sumedh.twinmind.app.domain.model.Transcript
import kotlinx.coroutines.flow.Flow

interface MeetingRepository {
    suspend fun startMeeting(title: String): Long

    suspend fun addTranscript(meetingId: Long, text: String, timestamp: Long)

    fun getTranscripts(meetingId: Long): Flow<List<Transcript>>

    fun getAllMeetings(): Flow<List<Meeting>>
}