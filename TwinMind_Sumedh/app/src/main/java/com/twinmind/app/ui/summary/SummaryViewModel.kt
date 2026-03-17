package com.twinmind.app.ui.summary

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.twinmind.app.data.db.entity.SummaryEntity
import com.twinmind.app.data.repository.StreamState
import com.twinmind.app.data.repository.SummaryRepository
import com.twinmind.app.data.repository.TranscriptRepository
import com.twinmind.app.work.WorkScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SummaryScreenState {
    data object Loading     : SummaryScreenState()
    data object Transcribing : SummaryScreenState()
    data class Streaming(val partial: SummaryEntity)  : SummaryScreenState()
    data class Complete(val summary: SummaryEntity)   : SummaryScreenState()
    data class Error(val message: String)             : SummaryScreenState()
}

@HiltViewModel
class SummaryViewModel @Inject constructor(
    savedStateHandle         : SavedStateHandle,
    private val summaryRepository    : SummaryRepository,
    private val transcriptRepository : TranscriptRepository,
    private val workScheduler        : WorkScheduler
) : ViewModel() {

    private val sessionId: String = checkNotNull(savedStateHandle["sessionId"])

    private val _screenState = MutableStateFlow<SummaryScreenState>(SummaryScreenState.Loading)
    val screenState: StateFlow<SummaryScreenState> = _screenState.asStateFlow()

    init {
        // Observe Room — single source of truth.
        // When SummaryWorker writes to DB, this collector fires automatically.
        viewModelScope.launch {
            summaryRepository.observeSummary(sessionId)
                .collect { entity ->
                    when {
                        entity == null        -> checkAndStartSummary()
                        entity.isComplete     -> _screenState.value =
                            SummaryScreenState.Complete(entity)
                        else                  -> _screenState.value =
                            SummaryScreenState.Streaming(entity)
                    }
                }
        }
    }

    private suspend fun checkAndStartSummary() {
        val transcript = transcriptRepository.getFullTranscript(sessionId)
        if (transcript.isNotBlank()) {
            // Transcript is ready — stream summary directly from ViewModel
            startStreaming(transcript)
        } else {
            // Transcript not ready yet — show Transcribing state and let
            // WorkManager handle it. Room observer will fire when done.
            _screenState.value = SummaryScreenState.Transcribing
            workScheduler.enqueueSummary(sessionId)
        }
    }

    private fun startStreaming(transcript: String) {
        viewModelScope.launch {
            summaryRepository.generateSummaryStream(sessionId, transcript)
                .collect { state ->
                    when (state) {
                        is StreamState.Loading  ->
                            _screenState.value = SummaryScreenState.Loading
                        is StreamState.Error    ->
                            _screenState.value = SummaryScreenState.Error(state.message)
                        is StreamState.Complete -> {
                            // Room observer handles transition to Complete
                        }
                    }
                }
        }
    }

    fun retry() {
        viewModelScope.launch {
            _screenState.value = SummaryScreenState.Loading
            checkAndStartSummary()
        }
    }
}