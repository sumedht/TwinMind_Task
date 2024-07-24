package com.sumedh.practiceproject.data.remote

import com.sumedh.practiceproject.data.dto.ResponseDto
import retrofit2.http.GET

interface TodoApi {
    @GET("/todos")
    suspend fun getTodoList(): ResponseDto
}