package com.sumedh.wayfair.presentation.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sumedh.wayfair.presentation.viewmodel.ProductViewModel

@Composable
fun ProductScreen(
    viewModel: ProductViewModel = hiltViewModel()
) {
    val productResultState = viewModel.productResultState.value

    Scaffold (
        topBar = {
            TopAppBar(
                backgroundColor = MaterialTheme.colors.primary,
                contentColor = Color.White,
                title = {
                    Text(text = "WayFair Product List")
                },
            )
        }
    ) {
        Box (
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn (
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(productResultState.products) { product ->
                        ProductListItem(product = product)
                        Divider(color = Color.LightGray, modifier = Modifier.padding(horizontal = 20.dp))
                    }
                }

                Spacer(modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.0f)
                    .background(Color.LightGray)
                )
            }

            if (productResultState.error.isNotBlank()) {
                Toast.makeText(LocalContext.current, productResultState.error, Toast.LENGTH_LONG).show()
            }

            if (productResultState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }

}