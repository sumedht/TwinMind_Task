package com.sumedh.twinmind.app.data.repository

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.DateTime
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.calendar.model.Event
import com.sumedh.twinmind.app.data.model.UpcomingEvent
import com.sumedh.twinmind.app.domain.repository.CalendarRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import com.sumedh.twinmind.app.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import javax.inject.Inject

class CalendarRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : CalendarRepository {

    override suspend fun getUpcomingEvents(): Result<List<UpcomingEvent>> = withContext(Dispatchers.IO) {
        try {
            // 1. Get the last signed-in Google account.
            val account = GoogleSignIn.getLastSignedInAccount(context)
            if (account == null) {
                return@withContext Result.Error(Exception("User not signed in or permissions not granted."))
            }

            // 2. Create credentials for the Calendar API.
            // The app must request the CALENDAR_READONLY scope during the Google Sign-In flow.
            val credential = GoogleAccountCredential.usingOAuth2(
                context, setOf(CalendarScopes.CALENDAR_READONLY)
            ).setSelectedAccount(account.account)

            // 3. Build the Calendar service.
            val calendarService = Calendar.Builder(
                NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                credential
            )
                .setApplicationName("TwinMind App")
                .build()

            // 4. Fetch events from the primary calendar.
            val now = DateTime(System.currentTimeMillis())
            val events = calendarService.events().list("primary")
                .setMaxResults(10) // Get the next 10 events
                .setTimeMin(now)
                .setOrderBy("startTime")
                .setSingleEvents(true) // Expand recurring events
                .execute()

            val upcomingEvents = events.items.mapNotNull { it.toUpcomingEvent() }
            Result.Success(upcomingEvents)

        } catch (e: Exception) {
            // This can happen due to network issues, permission errors, etc.
            Result.Error(e)
        }
    }

    /**
     * Extension function to map a Google Calendar API Event to our domain UpcomingEvent model.
     */
    private fun Event.toUpcomingEvent(): UpcomingEvent? {
        val eventId = this.id ?: return null
        val title = this.summary ?: "No Title"
        val startDateTime = this.start?.dateTime ?: this.start?.date ?: return null
        val endDateTime = this.end?.dateTime ?: this.end?.date ?: return null

        return UpcomingEvent(
            id = eventId,
            title = title,
            startTime = startDateTime.toOffsetDateTime(),
            endTime = endDateTime.toOffsetDateTime()
        )
    }

    /**
     * Converts a Google API DateTime to a modern java.time.OffsetDateTime.
     */
    private fun DateTime.toOffsetDateTime(): OffsetDateTime {
        return Instant.ofEpochMilli(this.value).atZone(ZoneId.systemDefault()).toOffsetDateTime()
    }
}