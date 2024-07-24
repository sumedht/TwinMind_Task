package com.sumedh.practiceproject.data.dto

import com.sumedh.practiceproject.domain.model.Todo

data class TodoDto(
    val completed: Boolean,
    val id: Int,
    val title: String,
    val userId: Int
)

fun TodoDto.toTodo(): Todo {
    return Todo(
        completed = completed,
        id = id,
        title = title,
        userId = userId
    )
}