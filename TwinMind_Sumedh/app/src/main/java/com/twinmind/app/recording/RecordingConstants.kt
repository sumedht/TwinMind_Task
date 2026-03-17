package com.twinmind.app.recording

object RecordingConstants {
    const val CHUNK_DURATION_MS = 30_000L
    const val OVERLAP_DURATION_MS = 2_000L
    const val SILENCE_THRESHOLD_AMPLITUDE = 200          // ~-60 dBFS
    const val SILENCE_DETECTION_WINDOW_MS = 10_000L
    const val SAMPLE_RATE = 16_000                       // 16 kHz for Whisper
    const val CHANNEL_CONFIG = android.media.AudioFormat.CHANNEL_IN_MONO
    const val AUDIO_FORMAT = android.media.AudioFormat.ENCODING_PCM_16BIT
    const val NOTIFICATION_ID = 1001
    const val NOTIFICATION_CHANNEL_ID = "recording_channel"
    const val NOTIFICATION_CHANNEL_NAME = "Recording"
    const val ACTION_STOP = "com.twinmind.app.ACTION_STOP"
    const val ACTION_RESUME = "com.twinmind.app.ACTION_RESUME"
    const val EXTRA_SESSION_ID = "session_id"
    const val MIN_FREE_STORAGE_BYTES = 50L * 1024 * 1024  // 50 MB
}