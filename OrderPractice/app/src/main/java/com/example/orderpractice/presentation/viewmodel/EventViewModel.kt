package com.example.orderpractice.presentation.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.orderpractice.common.Resource
import com.example.orderpractice.domain.use_case.GetTrackingByNumberUseCase
import com.example.orderpractice.presentation.EventResultState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class EventViewModel @Inject constructor(
    private val getTrackingByNumberUseCase: GetTrackingByNumberUseCase
) : ViewModel() {

    private val _eventResultState = mutableStateOf(EventResultState())
    val eventResultState: State<EventResultState> = _eventResultState

    init {
        getEvents()
    }

    private fun getEvents() {
        getTrackingByNumberUseCase.invoke().onEach { result ->
            when(result) {
                is Resource.Success -> {
                    _eventResultState.value = EventResultState(events = result.data?: emptyList())
                }

                is Resource.Error -> {
                    _eventResultState.value = EventResultState(error = result.message?:"An unexpected error occoured")
                }

                is Resource.Loading -> {
                    _eventResultState.value = EventResultState(isLoading = true)
                }
            }

        }.launchIn(viewModelScope)
    }
}