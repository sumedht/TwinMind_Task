package com.sumedh.twinmind.app.presentation.ui.meeting

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sumedh.twinmind.app.domain.usecase.AddTranscriptUseCase
import com.sumedh.twinmind.app.domain.usecase.GetTranscriptsUseCase
import com.sumedh.twinmind.app.domain.usecase.StartMeetingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MeetingViewModel @Inject constructor(
    private val startMeetingUseCase: StartMeetingUseCase,
    private val addTranscriptUseCase: AddTranscriptUseCase,
    private val getTranscriptsUseCase: GetTranscriptsUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(MeetingUiState())
    val uiState = _uiState.asStateFlow()

    private var meetingId: Long = -1
    private var timerJob: Job? = null

    init {
        // In a real app, you'd get the meetingId from navigation arguments
        // For now, we'll start a new meeting whenever this ViewModel is created.
        startNewMeeting("A really, really long title")
    }

    private fun startNewMeeting(title: String) {
        viewModelScope.launch {
            meetingId = startMeetingUseCase(title)
            _uiState.update { it.copy(meetingTitle = title, isRecording = true) }
            startTimer()
            // In a real app, you would start listening to the microphone here.
            // For now, we simulate receiving transcriptions.
            simulateTranscription()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.isRecording) {
                delay(1000)
                _uiState.update { it.copy(elapsedTime = it.elapsedTime + 1) }
            }
        }
    }

    private fun simulateTranscription() {
        viewModelScope.launch {
            delay(5000) // Simulate first transcript after 5s
            addTranscriptUseCase(meetingId, "TwinMind is listening in the background...", 5000)

            // Listen to the DB for updates
            getTranscriptsUseCase(meetingId).collect { transcripts ->
                _uiState.update { it.copy(transcripts = transcripts) }
            }
        }
    }

    fun stopMeeting() {
        _uiState.update { it.copy(isRecording = false) }
        timerJob?.cancel()
        // In a real app, you would stop listening to the microphone here.
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}