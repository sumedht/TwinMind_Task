package com.sumedh.twinmind.app.presentation.ui.meeting

import android.text.format.DateUtils.formatElapsedTime
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sumedh.twinmind.ui.theme.TwinMindTheme

@Composable
fun MeetingScreen(
    viewModel: MeetingViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            MeetingTopBar(
                elapsedTime = uiState.elapsedTime,
                onBackClick = onNavigateBack
            )
        },
        bottomBar = {
            MeetingBottomBar(onEndClick = { viewModel.stopMeeting() })
        }
    ) { padding ->
        MeetingContent(
            modifier = Modifier.padding(padding),
            uiState = uiState
        )
    }
}

@Composable
fun MeetingTopBar(elapsedTime: Long, onBackClick: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = formatElapsedTime(elapsedTime),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            IconButton(onClick = { /*TODO*/ }) {
                Icon(Icons.Default.MoreVert, contentDescription = "More Options")
            }
        },
        backgroundColor = MaterialTheme.colors.background,
        elevation = 0.dp
    )
}

@Composable
fun MeetingContent(modifier: Modifier = Modifier, uiState: MeetingUiState) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = uiState.meetingTitle,
            style = MaterialTheme.typography.h5,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(64.dp))

        // In a real app, this would show the live transcript.
        // For now, it shows the simulated message.
        if (uiState.transcripts.isNotEmpty()) {
            Text(
                text = uiState.transcripts.last().text,
                style = MaterialTheme.typography.body1,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (uiState.isRecording) {
            Button(
                onClick = { /* This is just a visual element */ },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFF0F0F0))
            ) {
                Text("Listening", color = Color.Gray)
            }
        }
    }
}

@Composable
fun MeetingBottomBar(onEndClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = { /*TODO: Chat with transcript*/ },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(50)
        ) {
            Text("Chat with Transcript")
        }
        Spacer(modifier = Modifier.width(16.dp))
        Button(
            onClick = onEndClick,
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red),
            shape = RoundedCornerShape(50)
        ) {
            Text("End", color = Color.White)
        }
    }
}

private fun formatElapsedTime(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return String.format("%02d:%02d:%02d", hours, minutes, secs)
}

@Preview(showBackground = true)
@Composable
fun MeetingScreenPreview() {
    TwinMindTheme {
        Scaffold(
            topBar = { MeetingTopBar(elapsedTime = 351, onBackClick = {}) },
            bottomBar = { MeetingBottomBar(onEndClick = {}) }
        ) { padding ->
            MeetingContent(
                modifier = Modifier.padding(padding),
                uiState = MeetingUiState(
                    meetingTitle = "Here is a really, really long title",
                    isRecording = true
                )
            )
        }
    }
}