package com.twinmind.app.recording.handler

import com.twinmind.app.recording.RecordingConstants
import javax.inject.Inject

class SilenceDetector @Inject constructor() {
    interface Listener { fun onSilenceDetected() }

    private var silenceStartMs: Long = 0L
    private var silenceReported = false
    private var listener: Listener? = null

    fun setListener(l: Listener) { listener = l }

    fun feed(buffer: ShortArray, bytesRead: Int) {
        val maxAmplitude = buffer.take(bytesRead / 2).maxOfOrNull { Math.abs(it.toInt()) } ?: 0
        if (maxAmplitude < RecordingConstants.SILENCE_THRESHOLD_AMPLITUDE) {
            if (silenceStartMs == 0L) silenceStartMs = System.currentTimeMillis()
            val silenceDuration = System.currentTimeMillis() - silenceStartMs
            if (silenceDuration >= RecordingConstants.SILENCE_DETECTION_WINDOW_MS && !silenceReported) {
                silenceReported = true
                listener?.onSilenceDetected()
            }
        } else {
            silenceStartMs = 0L
            silenceReported = false
        }
    }

    fun reset() {
        silenceStartMs = 0L
        silenceReported = false
    }
}