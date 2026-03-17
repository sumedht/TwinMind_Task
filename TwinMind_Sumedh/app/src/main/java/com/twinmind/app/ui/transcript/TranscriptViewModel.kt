package com.twinmind.app.ui.transcript

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.twinmind.app.data.db.entity.TranscriptEntity
import com.twinmind.app.data.repository.TranscriptRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class TranscriptUiState(
    val transcripts : List<TranscriptEntity> = emptyList(),
    val isLoading   : Boolean                = true
)

@HiltViewModel
class TranscriptViewModel @Inject constructor(
    savedStateHandle    : SavedStateHandle,
    transcriptRepository: TranscriptRepository
) : ViewModel() {

    private val sessionId: String = checkNotNull(savedStateHandle["sessionId"])

    val uiState: StateFlow<TranscriptUiState> =
        transcriptRepository.observeTranscripts(sessionId)
            .map { TranscriptUiState(transcripts = it, isLoading = false) }
            .stateIn(
                scope          = viewModelScope,
                started        = SharingStarted.WhileSubscribed(5_000),
                initialValue   = TranscriptUiState()
            )
}