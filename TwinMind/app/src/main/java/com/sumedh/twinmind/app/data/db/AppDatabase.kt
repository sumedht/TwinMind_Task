package com.sumedh.twinmind.app.data.db


import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.sumedh.twinmind.app.data.model.MeetingEntity
import com.sumedh.twinmind.app.data.model.TranscriptEntity

@Database(entities = [MeetingEntity::class, TranscriptEntity::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun meetingDao(): MeetingDao
    abstract fun transcriptDao(): TranscriptDao
}