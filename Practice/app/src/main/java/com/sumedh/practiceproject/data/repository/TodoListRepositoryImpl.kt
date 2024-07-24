package com.sumedh.practiceproject.data.repository

import com.sumedh.practiceproject.data.dto.ResponseDto
import com.sumedh.practiceproject.data.remote.TodoApi
import com.sumedh.practiceproject.domain.repository.TodoListRepository
import javax.inject.Inject

class TodoListRepositoryImpl @Inject constructor(
    private val api: TodoApi
): TodoListRepository {
    override suspend fun getTodoList(): ResponseDto {
        return api.getTodoList()
    }
}


