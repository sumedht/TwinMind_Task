package com.sumedh.wayfair.domain.repository

import com.sumedh.wayfair.data.dto.ProductDto

interface ProductsRepository {

    suspend fun getProducts(): List<ProductDto>
}