package com.twinmind.app.data.db.dao

import androidx.room.*
import com.twinmind.app.data.db.entity.RecordingSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordingSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: RecordingSessionEntity)

    @Update
    suspend fun update(session: RecordingSessionEntity)

    @Query("SELECT * FROM recording_sessions ORDER BY startTimeMs DESC")
    fun getAllSessions(): Flow<List<RecordingSessionEntity>>

    @Query("SELECT * FROM recording_sessions WHERE id = :id")
    suspend fun getById(id: String): RecordingSessionEntity?

    @Query("SELECT * FROM recording_sessions WHERE id = :id")
    fun observeById(id: String): Flow<RecordingSessionEntity?>

    @Query("SELECT * FROM recording_sessions WHERE status = 'RECORDING' OR status = 'PAUSED' LIMIT 1")
    suspend fun getActiveSession(): RecordingSessionEntity?

    @Query("UPDATE recording_sessions SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)

    @Query("UPDATE recording_sessions SET durationMs = :duration, endTimeMs = :endTime WHERE id = :id")
    suspend fun updateDuration(id: String, duration: Long, endTime: Long)

    @Query("UPDATE recording_sessions SET totalChunks = :total, transcribedChunks = :transcribed WHERE id = :id")
    suspend fun updateChunkProgress(id: String, total: Int, transcribed: Int)
}