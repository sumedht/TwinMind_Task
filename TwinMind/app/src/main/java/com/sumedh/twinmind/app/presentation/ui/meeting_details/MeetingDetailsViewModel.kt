package com.sumedh.twinmind.app.presentation.ui.meeting_details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sumedh.twinmind.app.domain.usecase.GetTranscriptsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MeetingDetailsViewModel @Inject constructor(
    private val getTranscriptsUseCase: GetTranscriptsUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(MeetingDetailsUiState())
    val uiState = _uiState.asStateFlow()

    private val meetingId: Long = savedStateHandle.get<Long>("meetingId") ?: -1

    init {
        if (meetingId != -1L) {
            loadMeetingDetails()
        } else {
            // Handle error case where meetingId is invalid
        }
    }

    private fun loadMeetingDetails() {
        viewModelScope.launch {
            // In a real app, you would fetch the meeting title and date from the repository
            // based on the meetingId. We'll use placeholders for now.
            _uiState.update {
                it.copy(
                    meetingTitle = "Deep Philosophical Discussions on Life, Technology, and Society at Dinner",
                    meetingDate = "May 24, 2025 - 9:07 PM - Sunnyvale, CA"
                )
            }

            // Fetch transcripts
            getTranscriptsUseCase(meetingId).collect { transcripts ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        transcripts = transcripts,
                        // For now, we'll use dummy data for summary and action items
                        summary = listOf("Meeting Placement Overview", "Another summary point"),
                        actionItems = listOf("Follow up with investor", "Research new market trends")
                    )
                }
            }
        }
    }
}