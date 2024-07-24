package com.sumedh.practiceproject.domain.repository

import com.sumedh.practiceproject.data.dto.ResponseDto

interface TodoListRepository {
    suspend fun getTodoList(): ResponseDto
}