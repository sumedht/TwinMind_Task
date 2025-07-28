package com.sumedh.twinmind.app.presentation.ui.meeting_details

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role.Companion.Button
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sumedh.twinmind.app.domain.model.Transcript
import com.sumedh.twinmind.ui.theme.TwinMindTheme
import java.util.concurrent.TimeUnit

@Composable
fun MeetingDetailsScreen(
    viewModel: MeetingDetailsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onChatClick: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) } // 0 for Summary, 1 for Transcript

    Scaffold(
        topBar = {
            MeetingDetailsTopBar(
                onBackClick = onNavigateBack
            )
        },
        bottomBar = {
            Button(
                onClick = { /* onChatClick(meetingId) */ },
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Text("Chat with Transcript")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(horizontal = 16.dp)) {
            item {
                Text(uiState.meetingTitle, style = MaterialTheme.typography.h5, fontWeight = FontWeight.Bold)
                Text(uiState.meetingDate, style = MaterialTheme.typography.body2, color = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                MeetingDetailsTabs(selectedTab = selectedTab, onTabSelected = { selectedTab = it })
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (selectedTab == 0) { // Summary View
                item {
                    Text("Summary", style = MaterialTheme.typography.h6, fontWeight = FontWeight.Bold)
                }
                items(uiState.summary) { point ->
                    Text("• $point", modifier = Modifier.padding(bottom = 4.dp))
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Action Items", style = MaterialTheme.typography.h6, fontWeight = FontWeight.Bold)
                }
                items(uiState.actionItems) { item ->
                    Text("• $item", modifier = Modifier.padding(bottom = 4.dp))
                }

            } else { // Transcript View
                items(uiState.transcripts) { transcript ->
                    TranscriptItem(transcript)
                }
            }
        }
    }
}

@Composable
fun MeetingDetailsTopBar(onBackClick: () -> Unit) {
    TopAppBar(
        title = { },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            IconButton(onClick = { /* TODO */ }) {
                Icon(Icons.Default.Share, contentDescription = "Share")
            }
        },
        backgroundColor = MaterialTheme.colors.background,
        elevation = 0.dp
    )
}

@Composable
fun MeetingDetailsTabs(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    val tabs = listOf("Summary", "Notes", "Transcript")
    TabRow(
        selectedTabIndex = if (selectedTab == 0) 0 else 2, // Map our state to the visual tabs
        backgroundColor = Color.Transparent,
        contentColor = MaterialTheme.colors.primary,
        divider = {}
    ) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = (selectedTab == 0 && index == 0) || (selectedTab == 1 && index == 2),
                onClick = {
                    if (index == 0) onTabSelected(0) // Summary
                    if (index == 2) onTabSelected(1) // Transcript
                    // "Notes" tab is visual only for now
                },
                text = { Text(title, fontWeight = if ((selectedTab == 0 && index == 0) || (selectedTab == 1 && index == 2)) FontWeight.Bold else FontWeight.Normal) }
            )
        }
    }
}

@Composable
fun TranscriptItem(transcript: Transcript) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(
            text = formatTranscriptTimestamp(transcript.timestamp),
            style = MaterialTheme.typography.body2,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colors.primary
        )
        Text(text = transcript.text, style = MaterialTheme.typography.body1)
    }
}

private fun formatTranscriptTimestamp(millis: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
    return String.format("%02d:%02d", minutes, seconds)
}


@Preview(showBackground = true)
@Composable
fun MeetingDetailsScreenPreview() {
    TwinMindTheme {
        MeetingDetailsScreen(onNavigateBack = {}, onChatClick = {})
    }
}