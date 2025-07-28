package com.sumedh.twinmind.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.sumedh.twinmind.app.data.model.TranscriptEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TranscriptDao {
    @Insert
    suspend fun insertTranscript(transcript: TranscriptEntity)

    @Query("SELECT * FROM transcripts WHERE meetingId = :meetingId ORDER BY timestamp ASC")
    fun getTranscriptsForMeeting(meetingId: Long): Flow<List<TranscriptEntity>>
}