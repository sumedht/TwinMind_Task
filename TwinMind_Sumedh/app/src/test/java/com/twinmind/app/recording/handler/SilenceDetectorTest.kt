package com.twinmind.app.recording.handler

import com.twinmind.app.recording.RecordingConstants
import io.mockk.*
import org.junit.Before
import org.junit.Test

class SilenceDetectorTest {

    private lateinit var detector : SilenceDetector
    private lateinit var listener : SilenceDetector.Listener

    @Before
    fun setup() {
        detector = SilenceDetector()
        listener = mockk(relaxed = true)
        detector.setListener(listener)
    }

    private fun silentBuffer() = ShortArray(1024) { 0 }
    private fun loudBuffer()   = ShortArray(1024) { (RecordingConstants.SILENCE_THRESHOLD_AMPLITUDE.toShort() + 500).toShort() }

    @Test
    fun `silence not reported before 10 seconds`() {
        // feed silent audio but don't advance past window
        val buf = silentBuffer()
        repeat(5) { detector.feed(buf, buf.size * 2) }
        verify(exactly = 0) { listener.onSilenceDetected() }
    }

    @Test
    fun `silence reported after continuous silent audio`() {
        // Manually poke the private field via reflection to simulate elapsed time
        val buf = silentBuffer()
        detector.feed(buf, buf.size * 2)  // starts the clock

        val field = SilenceDetector::class.java.getDeclaredField("silenceStartMs")
        field.isAccessible = true
        field.set(detector, System.currentTimeMillis() - 11_000L) // fast-forward 11s

        detector.feed(buf, buf.size * 2)
        verify(exactly = 1) { listener.onSilenceDetected() }
    }

    @Test
    fun `silence not reported after loud audio resets clock`() {
        val silent = silentBuffer()
        val loud   = loudBuffer()

        detector.feed(silent, silent.size * 2)
        val field = SilenceDetector::class.java.getDeclaredField("silenceStartMs")
        field.isAccessible = true
        field.set(detector, System.currentTimeMillis() - 11_000L)

        detector.feed(loud, loud.size * 2)   // resets
        detector.feed(silent, silent.size * 2)
        verify(exactly = 0) { listener.onSilenceDetected() }
    }

    @Test
    fun `silence reported only once even after multiple feeds`() {
        val buf = silentBuffer()
        detector.feed(buf, buf.size * 2)
        val field = SilenceDetector::class.java.getDeclaredField("silenceStartMs")
        field.isAccessible = true
        field.set(detector, System.currentTimeMillis() - 11_000L)
        repeat(5) { detector.feed(buf, buf.size * 2) }
        verify(exactly = 1) { listener.onSilenceDetected() }
    }

    @Test
    fun `reset clears silence state`() {
        val buf = silentBuffer()
        detector.feed(buf, buf.size * 2)
        detector.reset()
        val field = SilenceDetector::class.java.getDeclaredField("silenceStartMs")
        field.isAccessible = true
        assert(field.get(detector) == 0L)
    }
}