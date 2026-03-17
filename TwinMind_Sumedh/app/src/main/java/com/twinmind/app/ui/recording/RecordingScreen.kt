package com.twinmind.app.ui.recording

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.*
import com.twinmind.app.recording.service.RecordingState
import com.twinmind.app.ui.theme.*

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun RecordingScreen(
    sessionId    : String,
    onStop       : () -> Unit,
    onNavigateUp : () -> Unit,
    viewModel    : RecordingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // ── Build the permission list ─────────────────────────────────────────────
    val permissions = buildList {
        add(Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val permissionState = rememberMultiplePermissionsState(permissions) { result ->
        // Called when user responds to the permission dialog
        val allGranted = result.values.all { it }
        if (allGranted) {
            viewModel.onPermissionGranted(sessionId)
        }
    }

    // ── Launch permission request + service on first composition ─────────────
    LaunchedEffect(sessionId) {
        if (permissionState.allPermissionsGranted) {
            viewModel.startAndBind(sessionId)
        } else {
            permissionState.launchMultiplePermissionRequest()
        }
    }

    // ── Also start if permissions were just granted ───────────────────────────
    LaunchedEffect(permissionState.allPermissionsGranted) {
        if (permissionState.allPermissionsGranted &&
            uiState.recordingState == RecordingState.IDLE) {
            viewModel.startAndBind(sessionId)
        }
    }

    // ── Navigate away when stopped ────────────────────────────────────────────
    LaunchedEffect(uiState.recordingState) {
        if (uiState.recordingState == RecordingState.STOPPED) onStop()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recording", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        when {
            // ── Permission permanently denied ─────────────────────────────────
            permissionState.shouldShowRationale ||
                    permissionState.permissions.any {
                        !it.status.isGranted && !it.status.shouldShowRationale
                    } && !permissionState.allPermissionsGranted -> {
                PermissionDeniedContent(
                    modifier  = Modifier.padding(padding),
                    onRequest = { permissionState.launchMultiplePermissionRequest() }
                )
            }

            // ── Waiting for permission ────────────────────────────────────────
            !permissionState.allPermissionsGranted -> {
                Box(
                    Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = TwinMindPrimary)
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Requesting microphone permission...",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // ── Recording UI ──────────────────────────────────────────────────
            else -> {
                RecordingContent(
                    modifier  = Modifier.padding(padding),
                    uiState   = uiState,
                    onStop    = { viewModel.stopRecording() }
                )
            }
        }
    }
}

// ── Recording content (extracted for clarity) ─────────────────────────────────

@Composable
private fun RecordingContent(
    modifier : Modifier,
    uiState  : RecordingUiState,
    onStop   : () -> Unit
) {
    Column(
        modifier            = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterVertically)
    ) {
        PulsingMicIndicator(isRecording = uiState.recordingState == RecordingState.RECORDING)

        Text(
            text          = formatElapsed(uiState.elapsedMs),
            fontSize      = 52.sp,
            fontWeight    = FontWeight.Light,
            color         = MaterialTheme.colorScheme.onBackground,
            letterSpacing = 4.sp
        )

        StatusChip(state = uiState.recordingState, message = uiState.statusMessage)

        Spacer(Modifier.height(16.dp))

        FilledIconButton(
            onClick  = onStop,
            modifier = Modifier.size(72.dp),
            shape    = CircleShape,
            colors   = IconButtonDefaults.filledIconButtonColors(
                containerColor = RecordingRed,
                contentColor   = Color.White
            )
        ) {
            Icon(Icons.Default.Stop, contentDescription = "Stop", modifier = Modifier.size(32.dp))
        }

        Text("Tap to stop", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
    }
}

// ── Permission denied UI ───────────────────────────────────────────────────────

@Composable
private fun PermissionDeniedContent(
    modifier  : Modifier,
    onRequest : () -> Unit
) {
    Column(
        modifier            = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector        = Icons.Default.MicOff,
            contentDescription = null,
            modifier           = Modifier.size(64.dp),
            tint               = MaterialTheme.colorScheme.error
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text      = "Microphone permission required",
            style     = MaterialTheme.typography.titleMedium,
            color     = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text      = "TwinMind needs microphone access to record meetings. Please grant the permission to continue.",
            style     = MaterialTheme.typography.bodyMedium,
            color     = TextSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onRequest,
            colors  = ButtonDefaults.buttonColors(containerColor = TwinMindPrimary)
        ) {
            Text("Grant Permission")
        }
    }
}

// ── Reused composables (unchanged from before) ────────────────────────────────

@Composable
private fun PulsingMicIndicator(isRecording: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue  = 1f,
        targetValue   = if (isRecording) 1.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    Box(
        modifier         = Modifier
            .size(140.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(
                if (isRecording) RecordingRed.copy(alpha = 0.15f)
                else MaterialTheme.colorScheme.surfaceVariant
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier         = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(
                    if (isRecording) RecordingRed.copy(alpha = 0.25f)
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = Icons.Default.Mic,
                contentDescription = "Recording",
                tint               = if (isRecording) RecordingRed else TextSecondary,
                modifier           = Modifier.size(48.dp)
            )
        }
    }
}

@Composable
private fun StatusChip(state: RecordingState, message: String) {
    val (color, label) = when (state) {
        RecordingState.RECORDING          -> RecordingRed    to message
        RecordingState.PAUSED_PHONE_CALL  -> PausedAmber     to message
        RecordingState.PAUSED_AUDIO_FOCUS -> PausedAmber     to message
        RecordingState.STOPPED            -> SuccessGreen    to "Stopped"
        RecordingState.ERROR_STORAGE      -> MaterialTheme.colorScheme.error to message
        RecordingState.ERROR_SILENCE      -> PausedAmber     to message
        else                              -> TextSecondary   to message
    }
    AnimatedContent(
        targetState    = label,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label          = "statusChip"
    ) { text ->
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50.dp))
                .background(color.copy(alpha = 0.15f))
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (state == RecordingState.RECORDING) {
                    val blink by rememberInfiniteTransition(label = "blink").animateFloat(
                        initialValue  = 1f,
                        targetValue   = 0f,
                        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
                        label         = "blinkAlpha"
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(color.copy(alpha = blink))
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(text = text, color = color, fontWeight = FontWeight.Medium, fontSize = 14.sp)
            }
        }
    }
}

private fun formatElapsed(ms: Long): String {
    val secs = (ms / 1000).toInt()
    return "%02d:%02d".format(secs / 60, secs % 60)
}