package com.sumedh.wayfair.data.repository

import com.sumedh.wayfair.data.dto.ProductDto
import com.sumedh.wayfair.data.remote.ProductApi
import com.sumedh.wayfair.domain.repository.ProductsRepository
import javax.inject.Inject

class ProductsRepositoryImpl @Inject constructor(
    private val api: ProductApi
) : ProductsRepository {
    private var productList: List<ProductDto> = emptyList()
    override suspend fun getProducts(): List<ProductDto> {
        val response = api.getProducts()
        productList = response
        return productList
    }
}