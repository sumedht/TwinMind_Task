package com.twinmind.app.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class ChunkStatus { PENDING, UPLOADING, DONE, FAILED }

@Entity(
    tableName = "audio_chunks",
    foreignKeys = [ForeignKey(
        entity = RecordingSessionEntity::class,
        parentColumns = ["id"],
        childColumns = ["sessionId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("sessionId")]
)
data class AudioChunkEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sessionId: String,
    val chunkIndex: Int,
    val filePath: String,
    val durationMs: Long = 30_000L,
    val status: String = ChunkStatus.PENDING.name,
    val retryCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)