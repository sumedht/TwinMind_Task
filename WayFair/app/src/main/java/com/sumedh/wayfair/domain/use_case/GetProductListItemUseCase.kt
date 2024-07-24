package com.sumedh.wayfair.domain.use_case

import com.sumedh.wayfair.domain.model.Product
import com.sumedh.wayfair.domain.repository.ProductsRepository
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.Flow
import com.sumedh.wayfair.common.Resource
import com.sumedh.wayfair.data.dto.toProduct
import java.io.IOException
import javax.inject.Inject

class GetProductListItemUseCase @Inject constructor(
    private val repository: ProductsRepository
){
    fun invoke(): Flow<Resource<List<Product>>> = flow {
        try {
            emit(Resource.Loading())
            val products = repository.getProducts().map { it.toProduct() }
            emit(Resource.Success(products))
        } catch (e: retrofit2.HttpException) {
            emit(Resource.Error(e.localizedMessage?:"An unexpected error occurred"))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connectivity"))
        }
    }
}