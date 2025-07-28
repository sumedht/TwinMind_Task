package com.sumedh.twinmind.app.presentation.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.sumedh.twinmind.app.data.model.UpcomingEvent
import com.sumedh.twinmind.app.domain.model.Meeting
import com.sumedh.twinmind.app.domain.usecase.GetAllMeetingsUseCase
import com.sumedh.twinmind.app.domain.usecase.GetUpcomingEventsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.sumedh.twinmind.app.util.Result
import kotlinx.coroutines.flow.update
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getUpcomingEventsUseCase: GetUpcomingEventsUseCase,
    private val getAllMeetingsUseCase: GetAllMeetingsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        fetchUpcomingEvents()
        fetchMemories()
    }

    fun fetchUpcomingEvents() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                when (val result = getUpcomingEventsUseCase()) {
                    is Result.Success -> {
                        val groupedEvents = groupEventsByDate(result.data)
                        _uiState.update { it.copy(isLoading = false, calendarEventsByDate = groupedEvents) }
                    }
                    is Result.Error -> {
                        if (result.exception is UserRecoverableAuthIOException) {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    permissionRequestIntent = result.exception.intent
                                )
                            }
                        } else {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error = result.exception.message ?: "Failed to load events."
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun fetchMemories() {
        viewModelScope.launch {
            getAllMeetingsUseCase().collect { meetings ->
                val groupedMemories = groupMeetingsByDate(meetings)
                _uiState.update { it.copy(memoriesByDate = groupedMemories) }
            }
        }
    }

    fun onPermissionResult() {
        _uiState.update { it.copy(permissionRequestIntent = null) }
        fetchUpcomingEvents()
    }

    private fun groupMeetingsByDate(meetings: List<Meeting>): Map<String, List<Meeting>> {
        val formatter = DateTimeFormatter.ofPattern("E, MMM d", Locale.getDefault())
        return meetings.groupBy { it.createdAt.format(formatter) }
    }

    private fun groupEventsByDate(events: List<UpcomingEvent>): Map<String, List<UpcomingEvent>> {
        val formatter = DateTimeFormatter.ofPattern("E, MMM d", Locale.getDefault())
        return events.groupBy { it.startTime.format(formatter) }
    }
}