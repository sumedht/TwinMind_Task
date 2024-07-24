package com.sumedh.practiceproject.presentation

import com.sumedh.practiceproject.domain.model.Response
import com.sumedh.practiceproject.domain.model.Todo

data class TodoListState(
    val isLoading:Boolean = false,
    val todos: List<Todo> = emptyList(),
    val error: String = ""
)
