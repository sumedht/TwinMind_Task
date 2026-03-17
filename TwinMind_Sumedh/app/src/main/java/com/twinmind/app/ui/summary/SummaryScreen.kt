package com.twinmind.app.ui.summary

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twinmind.app.data.db.entity.SummaryEntity
import com.twinmind.app.ui.theme.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreen(
    sessionId        : String,
    onViewTranscript : () -> Unit,
    onNavigateUp     : () -> Unit,
    viewModel        : SummaryViewModel = hiltViewModel()
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Summary", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = onViewTranscript) {
                        Icon(
                            Icons.AutoMirrored.Filled.Article,
                            contentDescription = null,
                            tint   = TwinMindPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Transcript", color = TwinMindPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        AnimatedContent(
            targetState    = screenState,
            transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
            label          = "summaryContent",
            modifier       = Modifier.padding(padding)
        ) { state ->
            when (state) {
                is SummaryScreenState.Loading,
                is SummaryScreenState.Transcribing -> {
                    LoadingState(
                        message = if (state is SummaryScreenState.Transcribing)
                            "Transcribing audio..." else "Generating summary..."
                    )
                }
                is SummaryScreenState.Streaming -> {
                    SummaryContent(
                        summary     = state.partial,
                        isStreaming = true
                    )
                }
                is SummaryScreenState.Complete -> {
                    SummaryContent(
                        summary     = state.summary,
                        isStreaming = false
                    )
                }
                is SummaryScreenState.Error -> {
                    ErrorState(
                        message = state.message,
                        onRetry = viewModel::retry
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingState(message: String) {
    Column(
        modifier            = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(color = TwinMindPrimary)
        Spacer(Modifier.height(16.dp))
        Text(text = message, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier            = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text  = "Failed to generate summary",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(Modifier.height(8.dp))
        Text(text = message, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            colors  = ButtonDefaults.buttonColors(containerColor = TwinMindPrimary)
        ) {
            Text("Retry")
        }
    }
}

@Composable
private fun SummaryContent(summary: SummaryEntity, isStreaming: Boolean) {
    val gson        = remember { Gson() }
    val listType    = object : TypeToken<List<String>>() {}.type
    val actionItems = remember(summary.actionItems) {
        runCatching { gson.fromJson<List<String>>(summary.actionItems, listType) }
            .getOrDefault(emptyList())
    }
    val keyPoints   = remember(summary.keyPoints) {
        runCatching { gson.fromJson<List<String>>(summary.keyPoints, listType) }
            .getOrDefault(emptyList())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        if (summary.title.isNotBlank() && summary.title != "Generating...") {
            Text(
                text       = summary.title,
                style      = MaterialTheme.typography.headlineMedium,
                color      = MaterialTheme.colorScheme.onBackground
            )
        }

        if (isStreaming) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color    = TwinMindPrimary
            )
            Text(
                text  = "Generating summary...",
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // Summary section
        if (summary.summary.isNotBlank()) {
            SummarySection(
                title   = "Summary",
                accentColor = TwinMindPrimary
            ) {
                Text(
                    text  = summary.summary,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Action items
        if (actionItems.isNotEmpty()) {
            SummarySection(
                title       = "Action Items",
                accentColor = RecordingRed
            ) {
                actionItems.forEachIndexed { i, item ->
                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier          = Modifier.padding(vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 7.dp)
                                .size(7.dp)
                                .let {
                                    it.wrapContentSize()
                                }
                        ) {
                            Surface(
                                modifier = Modifier.size(7.dp),
                                shape    = RoundedCornerShape(50),
                                color    = RecordingRed
                            ) {}
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text  = item,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Key points
        if (keyPoints.isNotEmpty()) {
            SummarySection(
                title       = "Key Points",
                accentColor = SuccessGreen
            ) {
                keyPoints.forEachIndexed { _, point ->
                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier          = Modifier.padding(vertical = 4.dp)
                    ) {
                        Text(
                            text     = "•",
                            color    = SuccessGreen,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(end = 10.dp)
                        )
                        Text(
                            text  = point,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun SummarySection(
    title       : String,
    accentColor : Color,
    content     : @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier
                        .width(4.dp)
                        .height(18.dp),
                    shape    = RoundedCornerShape(2.dp),
                    color    = accentColor
                ) {}
                Spacer(Modifier.width(10.dp))
                Text(
                    text       = title,
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}