package com.sumedh.twinmind.app.presentation.ui.meeting

import com.sumedh.twinmind.app.domain.model.Transcript

data class MeetingUiState(
    val meetingTitle: String = "New Meeting",
    val transcripts: List<Transcript> = emptyList(),
    val isRecording: Boolean = false,
    val elapsedTime: Long = 0L,
    val error: String? = null
)