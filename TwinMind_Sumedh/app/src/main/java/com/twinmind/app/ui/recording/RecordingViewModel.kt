package com.twinmind.app.ui.recording

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.twinmind.app.recording.RecordingConstants.EXTRA_SESSION_ID
import com.twinmind.app.recording.service.AudioRecordingService
import com.twinmind.app.recording.service.RecordingState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecordingUiState(
    val recordingState      : RecordingState = RecordingState.IDLE,
    val elapsedMs           : Long           = 0L,
    val statusMessage       : String         = "Ready",
    val isBound             : Boolean        = false,
    val permissionRequired  : Boolean        = false   // ← new
)

@HiltViewModel
class RecordingViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecordingUiState())
    val uiState: StateFlow<RecordingUiState> = _uiState.asStateFlow()

    private var service: AudioRecordingService? = null

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            val s = (binder as AudioRecordingService.RecordingBinder).getService()
            service = s
            _uiState.update { it.copy(isBound = true) }
            viewModelScope.launch {
                s.recordingState.collect { state ->
                    _uiState.update { it.copy(recordingState = state) }
                }
            }
            viewModelScope.launch {
                s.elapsedMs.collect { ms ->
                    _uiState.update { it.copy(elapsedMs = ms) }
                }
            }
            viewModelScope.launch {
                s.statusMessage.collect { msg ->
                    _uiState.update { it.copy(statusMessage = msg) }
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            service = null
            _uiState.update { it.copy(isBound = false) }
        }
    }

    /** Called from the screen after permissions are granted */
    @RequiresApi(Build.VERSION_CODES.O)
    fun startAndBind(sessionId: String) {
        if (!hasRequiredPermissions()) {
            _uiState.update { it.copy(permissionRequired = true) }
            return
        }
        _uiState.update { it.copy(permissionRequired = false) }
        val intent = Intent(context, AudioRecordingService::class.java)
            .putExtra(EXTRA_SESSION_ID, sessionId)
        context.startForegroundService(intent)
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    fun stopRecording() {
        service?.stopRecording()
        unbind()
    }

    fun unbind() {
        runCatching { context.unbindService(connection) }
        service = null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun onPermissionGranted(sessionId: String) {
        startAndBind(sessionId)
    }

    override fun onCleared() {
        unbind()
        super.onCleared()
    }

    private fun hasRequiredPermissions(): Boolean {
        val audioGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        // POST_NOTIFICATIONS required on API 33+
        val notifGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else true

        return audioGranted && notifGranted
    }
}