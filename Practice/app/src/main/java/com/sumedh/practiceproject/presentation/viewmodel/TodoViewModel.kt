package com.sumedh.practiceproject.presentation.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sumedh.practiceproject.common.Resource
import com.sumedh.practiceproject.domain.use_case.GetTodoListUseCase
import com.sumedh.practiceproject.presentation.TodoListState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class TodoViewModel @Inject constructor(
    val getTodoListUseCase: GetTodoListUseCase
) : ViewModel() {

    private val _todoListState = mutableStateOf(TodoListState())
    val todoListState: State<TodoListState> = _todoListState


    init {
        getTodoList()
    }

    private fun getTodoList() {
        getTodoListUseCase().onEach { result ->
            when(result) {
                is Resource.Success -> {
                    _todoListState.value = TodoListState(todos = result.data?.result?: emptyList())
                }

                is Resource.Error -> {
                    _todoListState.value = TodoListState(error = result.message?:"An Unexpected error")
                }

                is Resource.Loading -> {
                    _todoListState.value = TodoListState(isLoading = true)
                }
            }
        }.launchIn(viewModelScope)
    }
}