package com.twinmind.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.twinmind.app.data.db.dao.*
import com.twinmind.app.data.db.entity.*

@Database(
    entities = [
        RecordingSessionEntity::class,
        AudioChunkEntity::class,
        TranscriptEntity::class,
        SummaryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class TwinMindDatabase : RoomDatabase() {
    abstract fun recordingSessionDao(): RecordingSessionDao
    abstract fun audioChunkDao(): AudioChunkDao
    abstract fun transcriptDao(): TranscriptDao
    abstract fun summaryDao(): SummaryDao
}