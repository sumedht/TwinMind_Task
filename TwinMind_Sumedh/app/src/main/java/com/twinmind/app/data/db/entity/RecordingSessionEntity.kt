package com.twinmind.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class SessionStatus { RECORDING, PAUSED, STOPPED, ERROR }

@Entity(tableName = "recording_sessions")
data class RecordingSessionEntity(
    @PrimaryKey val id: String,
    val title: String,
    val startTimeMs: Long,
    val endTimeMs: Long = 0L,
    val durationMs: Long = 0L,
    val status: String = SessionStatus.RECORDING.name,
    val totalChunks: Int = 0,
    val transcribedChunks: Int = 0,
    val summaryGenerated: Boolean = false
)