package com.sumedh.twinmind.app.presentation.ui.home

import android.content.Intent
import com.sumedh.twinmind.app.data.model.UpcomingEvent
import com.sumedh.twinmind.app.domain.model.Meeting

data class HomeUiState(
    val isLoading: Boolean = false,
    val calendarEventsByDate: Map<String, List<UpcomingEvent>> = emptyMap(),
    val error: String? = null,
    val memoriesByDate: Map<String, List<Meeting>> = emptyMap(),
    val permissionRequestIntent: Intent? = null
)