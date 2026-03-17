package com.twinmind.app.recording.service

import android.app.*
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.twinmind.app.MainActivity
import com.twinmind.app.R
import com.twinmind.app.data.db.dao.AudioChunkDao
import com.twinmind.app.data.db.dao.RecordingSessionDao
import com.twinmind.app.data.db.entity.AudioChunkEntity
import com.twinmind.app.data.db.entity.ChunkStatus
import com.twinmind.app.data.db.entity.RecordingSessionEntity
import com.twinmind.app.data.db.entity.SessionStatus
import com.twinmind.app.recording.RecordingConstants
import com.twinmind.app.recording.RecordingConstants.ACTION_RESUME
import com.twinmind.app.recording.RecordingConstants.ACTION_STOP
import com.twinmind.app.recording.RecordingConstants.AUDIO_FORMAT
import com.twinmind.app.recording.RecordingConstants.CHANNEL_CONFIG
import com.twinmind.app.recording.RecordingConstants.CHUNK_DURATION_MS
import com.twinmind.app.recording.RecordingConstants.EXTRA_SESSION_ID
import com.twinmind.app.recording.RecordingConstants.NOTIFICATION_CHANNEL_ID
import com.twinmind.app.recording.RecordingConstants.NOTIFICATION_CHANNEL_NAME
import com.twinmind.app.recording.RecordingConstants.NOTIFICATION_ID
import com.twinmind.app.recording.RecordingConstants.OVERLAP_DURATION_MS
import com.twinmind.app.recording.RecordingConstants.SAMPLE_RATE
import com.twinmind.app.recording.handler.AudioFocusHandler
import com.twinmind.app.recording.handler.HeadsetHandler
import com.twinmind.app.recording.handler.PhoneCallHandler
import com.twinmind.app.recording.handler.SilenceDetector
import com.twinmind.app.recording.handler.StorageHandler
import com.twinmind.app.work.WorkScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

enum class RecordingState {
    IDLE, RECORDING, PAUSED_PHONE_CALL, PAUSED_AUDIO_FOCUS,
    STOPPED, ERROR_STORAGE, ERROR_SILENCE
}

@AndroidEntryPoint
class AudioRecordingService : LifecycleService() {

    @Inject lateinit var sessionDao        : RecordingSessionDao
    @Inject lateinit var chunkDao          : AudioChunkDao
    @Inject lateinit var audioFocusHandler : AudioFocusHandler
    @Inject lateinit var phoneCallHandler  : PhoneCallHandler
    @Inject lateinit var headsetHandler    : HeadsetHandler
    @Inject lateinit var storageHandler    : StorageHandler
    @Inject lateinit var silenceDetector   : SilenceDetector
    @Inject lateinit var workScheduler     : WorkScheduler

    inner class RecordingBinder : Binder() {
        fun getService() = this@AudioRecordingService
    }

    private val binder = RecordingBinder()

    private val _recordingState = MutableStateFlow(RecordingState.IDLE)
    val recordingState: StateFlow<RecordingState> = _recordingState

    private val _elapsedMs = MutableStateFlow(0L)
    val elapsedMs: StateFlow<Long> = _elapsedMs

    private val _statusMessage = MutableStateFlow("Ready")
    val statusMessage: StateFlow<String> = _statusMessage

    private var sessionId      = ""
    private var audioRecord    : AudioRecord? = null
    private var recordingJob   : Job? = null
    private var timerJob       : Job? = null
    private var chunkIndex     = 0
    private var sessionStartMs = 0L

    private val bufferSize = AudioRecord.getMinBufferSize(
        SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT
    ).coerceAtLeast(8192)

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        registerHandlers()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            ACTION_STOP   -> stopRecording()
            ACTION_RESUME -> resumeRecording()
            else -> {
                val sid = intent?.getStringExtra(EXTRA_SESSION_ID)
                    ?: UUID.randomUUID().toString()
                startRecording(sid)
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    override fun onDestroy() {
        lifecycleScope.launch {
            if (_recordingState.value == RecordingState.RECORDING ||
                _recordingState.value.name.startsWith("PAUSED")
            ) {
                finalizeCurrentChunk()
                sessionDao.updateStatus(sessionId, SessionStatus.STOPPED.name)
                workScheduler.enqueueTranscript(sessionId)
            }
        }
        stopAllHandlers()
        super.onDestroy()
    }

    // ── Start / Stop / Pause / Resume ─────────────────────────────────────────

    private fun startRecording(sid: String) {
        if (!storageHandler.hasEnoughStorage()) {
            updateState(RecordingState.ERROR_STORAGE, "Recording stopped - Low storage")
            stopSelf()
            return
        }
        sessionId      = sid
        sessionStartMs = System.currentTimeMillis()
        chunkIndex     = 0

        lifecycleScope.launch {
            sessionDao.insert(
                RecordingSessionEntity(
                    id          = sessionId,
                    title       = "Meeting ${
                        SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
                            .format(Date())
                    }",
                    startTimeMs = sessionStartMs,
                    status      = SessionStatus.RECORDING.name
                )
            )
        }
        updateState(RecordingState.RECORDING, "Recording...")
        startForegroundWithNotification()
        startTimerLoop()
        launchRecordingLoop()
    }

    fun stopRecording() {
        recordingJob?.cancel()
        timerJob?.cancel()
        lifecycleScope.launch {
            finalizeCurrentChunk()
            val duration = System.currentTimeMillis() - sessionStartMs
            sessionDao.updateDuration(sessionId, duration, System.currentTimeMillis())
            sessionDao.updateStatus(sessionId, SessionStatus.STOPPED.name)
            workScheduler.enqueueTranscript(sessionId)
            updateState(RecordingState.STOPPED, "Stopped")
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun pauseRecording(state: RecordingState, message: String) {
        if (_recordingState.value != RecordingState.RECORDING) return
        recordingJob?.cancel()
        lifecycleScope.launch {
            finalizeCurrentChunk()
            sessionDao.updateStatus(sessionId, SessionStatus.PAUSED.name)
        }
        updateState(state, message)
        updateNotification(message)
    }

    private fun resumeRecording() {
        if (_recordingState.value != RecordingState.PAUSED_PHONE_CALL &&
            _recordingState.value != RecordingState.PAUSED_AUDIO_FOCUS
        ) return
        if (!storageHandler.hasEnoughStorage()) {
            updateState(RecordingState.ERROR_STORAGE, "Recording stopped - Low storage")
            stopSelf()
            return
        }
        lifecycleScope.launch {
            sessionDao.updateStatus(sessionId, SessionStatus.RECORDING.name)
        }
        updateState(RecordingState.RECORDING, "Recording...")
        launchRecordingLoop()
    }

    // ── Core recording loop ───────────────────────────────────────────────────

    private fun launchRecordingLoop() {
        recordingJob = lifecycleScope.launch(Dispatchers.IO) {
            val record = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                bufferSize
            )
            audioRecord = record
            record.startRecording()
            silenceDetector.reset()

            val bytesPerMs   = (SAMPLE_RATE * 2) / 1000
            var overlapBuffer = ByteArray(0)

            while (isActive && _recordingState.value == RecordingState.RECORDING) {
                if (!storageHandler.hasEnoughStorage()) {
                    withContext(Dispatchers.Main) {
                        updateState(RecordingState.ERROR_STORAGE, "Recording stopped - Low storage")
                        stopRecording()
                    }
                    break
                }

                val file = storageHandler.getChunkFile(sessionId, chunkIndex)
                val fos  = FileOutputStream(file)

                if (overlapBuffer.isNotEmpty()) fos.write(overlapBuffer)

                val chunkBuffer  = ByteArray(bufferSize)
                val shortBuf     = ShortArray(bufferSize / 2)
                var writtenBytes = overlapBuffer.size.toLong()
                val targetBytes  = CHUNK_DURATION_MS * bytesPerMs

                while (isActive &&
                    writtenBytes < targetBytes &&
                    _recordingState.value == RecordingState.RECORDING
                ) {
                    val read = record.read(chunkBuffer, 0, bufferSize)
                    if (read > 0) {
                        fos.write(chunkBuffer, 0, read)
                        writtenBytes += read
                        val shortRead = read / 2
                        for (i in 0 until shortRead) {
                            shortBuf[i] = ((chunkBuffer[i * 2].toInt() and 0xFF) or
                                    (chunkBuffer[i * 2 + 1].toInt() shl 8)).toShort()
                        }
                        silenceDetector.feed(shortBuf, read)
                    }
                }
                fos.flush()
                fos.close()

                val overlapBytes = (OVERLAP_DURATION_MS * bytesPerMs).toInt()
                overlapBuffer = if (file.length() >= overlapBytes)
                    file.readBytes().takeLast(overlapBytes).toByteArray()
                else ByteArray(0)

                val entity = AudioChunkEntity(
                    sessionId  = sessionId,
                    chunkIndex = chunkIndex,
                    filePath   = file.absolutePath,
                    status     = ChunkStatus.PENDING.name
                )
                val chunkId = chunkDao.insert(entity)
                workScheduler.enqueueChunk(sessionId, chunkId.toInt())
                chunkIndex++
            }

            record.stop()
            record.release()
            audioRecord = null
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private suspend fun finalizeCurrentChunk() {
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
        val file = storageHandler.getChunkFile(sessionId, chunkIndex)
        if (file.exists() && file.length() > 0) {
            val entity = AudioChunkEntity(
                sessionId  = sessionId,
                chunkIndex = chunkIndex,
                filePath   = file.absolutePath,
                status     = ChunkStatus.PENDING.name
            )
            chunkDao.insert(entity)
            chunkIndex++
        }
    }

    private fun startTimerLoop() {
        timerJob = lifecycleScope.launch {
            while (isActive) {
                _elapsedMs.value = System.currentTimeMillis() - sessionStartMs
                if (_recordingState.value == RecordingState.RECORDING) {
                    updateNotification(_statusMessage.value)
                }
                delay(1000)
            }
        }
    }

    private fun updateState(state: RecordingState, message: String) {
        _recordingState.value = state
        _statusMessage.value  = message
    }

    // ── Handlers ──────────────────────────────────────────────────────────────

    private fun registerHandlers() {
        phoneCallHandler.register(object : PhoneCallHandler.Listener {
            override fun onCallStarted() =
                pauseRecording(RecordingState.PAUSED_PHONE_CALL, "Paused - Phone call")
            override fun onCallEnded() = resumeRecording()
        })

        audioFocusHandler.register(object : AudioFocusHandler.Listener {
            override fun onAudioFocusLost() =
                pauseRecording(RecordingState.PAUSED_AUDIO_FOCUS, "Paused - Audio focus lost")
            override fun onAudioFocusGained() = resumeRecording()
        })

        headsetHandler.register(object : HeadsetHandler.Listener {
            override fun onHeadsetSourceChanged(description: String) {
                updateNotification("Mic source: $description")
            }
        })

        silenceDetector.setListener(object : SilenceDetector.Listener {
            override fun onSilenceDetected() {
                _statusMessage.value = "No audio detected - Check microphone"
                updateNotification("No audio detected - Check microphone")
            }
        })
    }

    private fun stopAllHandlers() {
        phoneCallHandler.unregister()
        audioFocusHandler.unregister()
        headsetHandler.unregister()
    }

    // ── Notification ──────────────────────────────────────────────────────────

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply { description = "Audio recording status" }
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    private fun buildNotification(message: String): Notification {
        val tapIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra(EXTRA_SESSION_ID, sessionId)
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val stopIntent = PendingIntent.getService(
            this, 1,
            Intent(this, AudioRecordingService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE
        )
        val resumeIntent = PendingIntent.getService(
            this, 2,
            Intent(this, AudioRecordingService::class.java).setAction(ACTION_RESUME),
            PendingIntent.FLAG_IMMUTABLE
        )
        val elapsed  = _elapsedMs.value
        val timer    = "%02d:%02d".format(elapsed / 60_000, (elapsed % 60_000) / 1000)
        val isPaused = _recordingState.value.name.startsWith("PAUSED")

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("TwinMind — $timer")
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_mic)
            .setContentIntent(tapIntent)
            .setOngoing(true)
            .addAction(R.drawable.ic_stop, "Stop", stopIntent)
            .apply {
                if (isPaused) addAction(R.drawable.ic_play, "Resume", resumeIntent)
            }
            .build()
    }

    private fun startForegroundWithNotification() {
        val notification = buildNotification("Recording...")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun updateNotification(message: String) {
        getSystemService(NotificationManager::class.java)
            .notify(NOTIFICATION_ID, buildNotification(message))
    }
}