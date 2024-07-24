package com.example.orderpractice.presentation.components

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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.orderpractice.presentation.viewmodel.EventViewModel

@Composable
fun TrackingScreen(
    viewModel: EventViewModel = hiltViewModel()
) {
    val eventResultState = viewModel.eventResultState.value

    Scaffold (
        topBar = {
            TopAppBar(
                backgroundColor = MaterialTheme.colors.primary,
                contentColor =  Color.White,
                title = {
                    Text(text = "Order tracking practice")
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
                    items(eventResultState.events) { event ->
                        EventListItem(event = event)
                        Divider(color = Color.LightGray, modifier = Modifier.padding(horizontal = 20.dp))
                    }
                }

                Spacer(modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.0f)
                    .background(Color.LightGray)
                )
            }

            if (eventResultState.error.isNotBlank()) {
                Toast.makeText(LocalContext.current, eventResultState.error, Toast.LENGTH_LONG).show()
            }

            if (eventResultState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }

}