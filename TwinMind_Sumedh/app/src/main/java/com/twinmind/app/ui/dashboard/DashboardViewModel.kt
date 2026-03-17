package com.twinmind.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.twinmind.app.data.db.entity.RecordingSessionEntity
import com.twinmind.app.data.repository.AudioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.util.UUID
import javax.inject.Inject

data class DashboardUiState(
    val sessions: List<RecordingSessionEntity> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val audioRepository: AudioRepository
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> =
        audioRepository.observeAllSessions()
            .map { DashboardUiState(sessions = it, isLoading = false) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = DashboardUiState()
            )

    fun generateNewSessionId(): String = UUID.randomUUID().toString()
}