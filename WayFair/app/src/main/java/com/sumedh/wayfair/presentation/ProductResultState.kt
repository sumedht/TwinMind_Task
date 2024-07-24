package com.sumedh.wayfair.presentation

import com.sumedh.wayfair.domain.model.Product

data class ProductResultState(
    val isLoading: Boolean = false,
    val products: List<Product> = emptyList(),
    val error: String = ""
)
