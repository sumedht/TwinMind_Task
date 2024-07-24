package com.rudderstack.analytics.data.source

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.rudderstack.analytics.data.MapTypeConverter
import com.rudderstack.analytics.data.model.Event


@Database(entities = [Event::class], version = 1)
@TypeConverters(MapTypeConverter::class)
internal abstract class EventDatabase: RoomDatabase() {
    abstract fun eventDao(): EventDao

    companion object {
        @Volatile
        private var dbInstance: EventDatabase? = null

        fun getInstance(context: Context): EventDatabase {
            return dbInstance ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EventDatabase::class.java,
                    "rudder_events_db"
                ).build()
                dbInstance = instance
                return instance
            }
        }
    }
}