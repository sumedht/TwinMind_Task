package com.example.orderpractice.presentation

import com.example.orderpractice.domain.model.Event

data class EventResultState(
    val isLoading: Boolean = false,
    val events: List<Event> = emptyList(),
    val error: String = ""
)