package com.twinmind.app.data.db.dao

import androidx.room.*
import com.twinmind.app.data.db.entity.TranscriptEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TranscriptDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transcript: TranscriptEntity)

    @Query("SELECT * FROM transcripts WHERE sessionId = :sessionId ORDER BY chunkIndex ASC")
    fun getTranscriptsForSession(sessionId: String): Flow<List<TranscriptEntity>>

    @Query("SELECT * FROM transcripts WHERE sessionId = :sessionId ORDER BY chunkIndex ASC")
    suspend fun getTranscriptsSync(sessionId: String): List<TranscriptEntity>

    @Query("DELETE FROM transcripts WHERE sessionId = :sessionId")
    suspend fun deleteAllForSession(sessionId: String)

    @Query("SELECT COUNT(*) FROM transcripts WHERE sessionId = :sessionId")
    suspend fun getCountForSession(sessionId: String): Int
}