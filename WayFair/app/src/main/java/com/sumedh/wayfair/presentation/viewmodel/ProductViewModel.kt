package com.sumedh.wayfair.presentation.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sumedh.wayfair.common.Resource
import com.sumedh.wayfair.data.dto.ResponseDto
import com.sumedh.wayfair.domain.use_case.GetProductListItemUseCase
import com.sumedh.wayfair.presentation.ProductResultState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val getProductListItemUseCase: GetProductListItemUseCase
) : ViewModel() {

    private val _productResultState = mutableStateOf(ProductResultState())
    val productResultState: State<ProductResultState> = _productResultState

    init {
        getProductList()
    }

    private fun getProductList() {
        getProductListItemUseCase.invoke().onEach { result ->
            when(result) {
                is Resource.Success -> {
                    _productResultState.value = ProductResultState(products = result.data?: emptyList())
                }

                is Resource.Error -> {
                    _productResultState.value = ProductResultState(error = result.message ?: "An unexpected error occoured")
                }

                is Resource.Loading -> {
                    _productResultState.value = ProductResultState(isLoading = true)
                }
            }
        }.launchIn(viewModelScope)
    }
}