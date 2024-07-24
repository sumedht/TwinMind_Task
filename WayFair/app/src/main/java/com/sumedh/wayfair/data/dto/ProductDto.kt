package com.sumedh.wayfair.data.dto

import com.sumedh.wayfair.common.Utils
import com.sumedh.wayfair.domain.model.Product

data class ProductDto(
    val date: String,
    val name: String,
    val rating: Double,
    val tagline: String
)

fun ProductDto.toProduct(): Product {
    return Product(
        date =  Utils.getReadableDate(date)?:"",
        name = name,
        rating = Utils.roundToNearest(rating),
        tagline = tagline
    )
}