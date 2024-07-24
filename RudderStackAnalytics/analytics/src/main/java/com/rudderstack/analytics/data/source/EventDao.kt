package com.rudderstack.analytics.data.source

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.rudderstack.analytics.data.model.Event

@Dao
internal interface EventDao {
    @Insert
    suspend fun insertEvent(event: Event)

    @Query("SELECT * FROM events")
    suspend fun getAllEvents(): List<Event>

    @Delete
    suspend fun deleteEvent(event: Event)
}