package com.sumedh.wayfair.data.remote

import com.sumedh.wayfair.data.dto.ResponseDto
import retrofit2.http.GET

interface ProductApi {

    @GET("interview-sandbox/android/json-to-list/products.v1.json")
    suspend fun getProducts(): ResponseDto
}