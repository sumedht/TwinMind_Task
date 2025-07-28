package com.sumedh.twinmind.app.data.repository

import com.sumedh.twinmind.app.data.db.MeetingDao
import com.sumedh.twinmind.app.data.db.TranscriptDao
import com.sumedh.twinmind.app.data.model.MeetingEntity
import com.sumedh.twinmind.app.data.model.TranscriptEntity
import com.sumedh.twinmind.app.domain.model.Meeting
import com.sumedh.twinmind.app.domain.model.Transcript
import com.sumedh.twinmind.app.domain.repository.MeetingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.OffsetDateTime
import javax.inject.Inject

class MeetingRepositoryImpl @Inject constructor(
    private val meetingDao: MeetingDao,
    private val transcriptDao: TranscriptDao
) : MeetingRepository {

    override suspend fun startMeeting(title: String): Long {
        val meetingEntity = MeetingEntity(
            title = title,
            createdAt = OffsetDateTime.now()
        )
        return meetingDao.insertMeeting(meetingEntity)
    }

    override suspend fun addTranscript(meetingId: Long, text: String, timestamp: Long) {
        val transcriptEntity = TranscriptEntity(
            meetingId = meetingId,
            text = text,
            timestamp = timestamp
        )
        transcriptDao.insertTranscript(transcriptEntity)
    }

    override fun getTranscripts(meetingId: Long): Flow<List<Transcript>> {
        return transcriptDao.getTranscriptsForMeeting(meetingId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getAllMeetings(): Flow<List<Meeting>> {
        return meetingDao.getAllMeetings().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    // --- Mappers ---
    private fun MeetingEntity.toDomainModel(): Meeting = Meeting(
        id = this.id,
        title = this.title,
        createdAt = this.createdAt
    )

    private fun TranscriptEntity.toDomainModel(): Transcript = Transcript(
        id = this.id,
        meetingId = this.meetingId,
        text = this.text,
        timestamp = this.timestamp
    )
}