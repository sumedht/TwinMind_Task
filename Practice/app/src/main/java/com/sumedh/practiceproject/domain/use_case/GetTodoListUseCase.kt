package com.sumedh.practiceproject.domain.use_case

import com.sumedh.practiceproject.common.Resource
import com.sumedh.practiceproject.data.dto.toTodo
import com.sumedh.practiceproject.domain.model.Response
import com.sumedh.practiceproject.domain.repository.TodoListRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetTodoListUseCase @Inject constructor(
    private val repository: TodoListRepository
){
    operator fun invoke(): Flow<Resource<Response>> = flow {
        emit(Resource.Loading())
        val result = repository.getTodoList().result.map { it.toTodo() }
        val finalResponse = Response(result)

        if (result.isNotEmpty()) {
            emit(Resource.Success(finalResponse))
        }
        else {
            emit(Resource.Error("Unable to load data"))
        }
    }
}