package com.sumedh.twinmind.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.sumedh.twinmind.app.data.model.MeetingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MeetingDao {
    @Insert
    suspend fun insertMeeting(meeting: MeetingEntity): Long

    @Query("SELECT * FROM meetings ORDER BY createdAt DESC")
    fun getAllMeetings(): Flow<List<MeetingEntity>>
}