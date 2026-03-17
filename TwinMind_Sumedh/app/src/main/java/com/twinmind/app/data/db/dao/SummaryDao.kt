package com.twinmind.app.data.db.dao

import androidx.room.*
import com.twinmind.app.data.db.entity.SummaryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SummaryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(summary: SummaryEntity): Long

    @Update
    suspend fun update(summary: SummaryEntity)

    @Query("SELECT * FROM summaries WHERE sessionId = :sessionId")
    fun observeSummary(sessionId: String): Flow<SummaryEntity?>

    @Query("SELECT * FROM summaries WHERE sessionId = :sessionId")
    suspend fun getSummarySync(sessionId: String): SummaryEntity?

    @Upsert
    fun upsert(summary: SummaryEntity)
}