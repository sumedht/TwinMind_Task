package com.twinmind.app.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "summaries",
    foreignKeys = [ForeignKey(
        entity = RecordingSessionEntity::class,
        parentColumns = ["id"],
        childColumns = ["sessionId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("sessionId", unique = true)]
)
data class SummaryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sessionId: String,
    val title: String = "",
    val summary: String = "",
    val actionItems: String = "",   // JSON array string
    val keyPoints: String = "",     // JSON array string
    val isComplete: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)