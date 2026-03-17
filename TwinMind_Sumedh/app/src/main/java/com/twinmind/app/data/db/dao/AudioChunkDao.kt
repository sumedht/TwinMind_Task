package com.twinmind.app.data.db.dao

import androidx.room.*
import com.twinmind.app.data.db.entity.AudioChunkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AudioChunkDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(chunk: AudioChunkEntity): Long

    @Update
    suspend fun update(chunk: AudioChunkEntity)

    @Query("SELECT * FROM audio_chunks WHERE sessionId = :sessionId ORDER BY chunkIndex ASC")
    fun getChunksForSession(sessionId: String): Flow<List<AudioChunkEntity>>

    @Query("SELECT * FROM audio_chunks WHERE sessionId = :sessionId ORDER BY chunkIndex ASC")
    suspend fun getChunksForSessionSync(sessionId: String): List<AudioChunkEntity>

    @Query("SELECT * FROM audio_chunks WHERE sessionId = :sessionId AND status != 'DONE' ORDER BY chunkIndex ASC")
    suspend fun getPendingChunks(sessionId: String): List<AudioChunkEntity>

    @Query("UPDATE audio_chunks SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Int, status: String)

    @Query("UPDATE audio_chunks SET retryCount = retryCount + 1 WHERE id = :id")
    suspend fun incrementRetry(id: Int)

    @Query("SELECT COUNT(*) FROM audio_chunks WHERE sessionId = :sessionId")
    suspend fun getChunkCount(sessionId: String): Int

    @Query("DELETE FROM audio_chunks WHERE id = :id")
    suspend fun deleteById(id: Int)
}