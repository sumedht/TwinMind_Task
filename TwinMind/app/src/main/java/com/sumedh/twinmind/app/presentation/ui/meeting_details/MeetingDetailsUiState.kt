package com.sumedh.twinmind.app.presentation.ui.meeting_details

import com.sumedh.twinmind.app.domain.model.Transcript

data class MeetingDetailsUiState(
    val meetingTitle: String = "",
    val meetingDate: String = "",
    val transcripts: List<Transcript> = emptyList(),
    val summary: List<String> = emptyList(), // Placeholder for summary points
    val actionItems: List<String> = emptyList(), // Placeholder for action items
    val isLoading: Boolean = true
)