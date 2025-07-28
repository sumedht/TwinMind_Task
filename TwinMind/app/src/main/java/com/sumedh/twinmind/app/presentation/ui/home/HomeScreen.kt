package com.sumedh.twinmind.app.presentation.ui.home

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sumedh.twinmind.app.data.model.UpcomingEvent
import com.sumedh.twinmind.app.domain.model.Meeting
import com.sumedh.twinmind.ui.theme.TwinMindTheme
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onCaptureClick: () -> Unit,
    onMeetingClick: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) } // 0 for Memories, 1 for Calendar

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.onPermissionResult()
        }
    }

    LaunchedEffect(uiState.permissionRequestIntent) {
        uiState.permissionRequestIntent?.let { intent ->
            permissionLauncher.launch(intent)
        }
    }

    Scaffold(
        topBar = { HomeTopBar() },
        floatingActionButton = { BottomCaptureBar(onCaptureClick = onCaptureClick) },
        floatingActionButtonPosition = FabPosition.Center
    ) { padding ->
        HomeContent(
            modifier = Modifier.padding(padding),
            uiState = uiState,
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
            onMeetingClick = onMeetingClick
        )
    }
}

@Composable
fun HomeTopBar() {
    TopAppBar(
        title = { Text("TwinMind", fontWeight = FontWeight.Bold) },
        backgroundColor = MaterialTheme.colors.background,
        elevation = 0.dp,
        actions = {
            Button(
                onClick = { /*TODO*/ },
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFE0E0E0))
            ) {
                Text("PRO", color = Color.Black)
            }
            IconButton(onClick = { /*TODO*/ }) {
                Icon(Icons.Default.HelpOutline, contentDescription = "Help")
            }
        }
    )
}

@Composable
fun HomeContent(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onMeetingClick: (Long) -> Unit
) {
    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        BrainProgressBar()
        HomeTabs(selectedTab = selectedTab, onTabSelected = onTabSelected)
        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = uiState.error, color = MaterialTheme.colors.error)
            }
        } else {
            if (selectedTab == 0) {
                MemoriesList(
                    memoriesByDate = uiState.memoriesByDate,
                    onMeetingClick = onMeetingClick
                )
            } else {
                EventList(eventsByDate = uiState.calendarEventsByDate)
            }
        }
    }
}

@Composable
fun BrainProgressBar() {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Building Your Second Brain", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = 0.72f,
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text("721 / 1000 hours", style = MaterialTheme.typography.caption)
        }
    }
}

@Composable
fun HomeTabs(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    val tabs = listOf("Memories", "Calendar", "Questions")
    TabRow(
        selectedTabIndex = selectedTab,
        backgroundColor = Color.Transparent,
        contentColor = MaterialTheme.colors.primary,
        divider = {}
    ) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
            )
        }
    }
}

@Composable
fun MemoriesList(
    memoriesByDate: Map<String, List<Meeting>>,
    onMeetingClick: (Long) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        memoriesByDate.forEach { (date, meetings) ->
            item {
                Text(date, style = MaterialTheme.typography.body2, fontWeight = FontWeight.Bold)
            }
            items(meetings) { meeting ->
                MeetingItem(meeting, onClick = { onMeetingClick(meeting.id) })
            }
        }
    }
}

@Composable
fun MeetingItem(meeting: Meeting, onClick: () -> Unit) {
    Row(
        modifier = Modifier.clickable(onClick = onClick).fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.LightGray)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(meeting.title, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun EventList(eventsByDate: Map<String, List<UpcomingEvent>>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        eventsByDate.forEach { (date, events) ->
            item {
                Text(date, style = MaterialTheme.typography.body2, fontWeight = FontWeight.Bold)
            }
            items(events) { event ->
                EventItem(event)
            }
        }
    }
}

@Composable
fun EventItem(event: UpcomingEvent) {
    val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")
    val startTime = event.startTime.format(timeFormatter)
    val endTime = event.endTime.format(timeFormatter)

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.LightGray)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(event.title, fontWeight = FontWeight.SemiBold)
            Text(
                "$startTime - $endTime",
                style = MaterialTheme.typography.body2,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun BottomCaptureBar(onCaptureClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(50),
        elevation = 4.dp,
        modifier = Modifier.padding(bottom = 16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TextButton(onClick = { /*TODO*/ }) {
                Icon(Icons.Default.Search, contentDescription = "Ask")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Ask All Memories")
            }
            Divider(
                modifier = Modifier.height(30.dp).width(1.dp)
            )
            Button(
                onClick = onCaptureClick,
                shape = CircleShape,
                modifier = Modifier.size(50.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(Icons.Default.Mic, contentDescription = "Capture")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    TwinMindTheme {
        HomeScreen(onCaptureClick = {}, onMeetingClick = {})
    }
}