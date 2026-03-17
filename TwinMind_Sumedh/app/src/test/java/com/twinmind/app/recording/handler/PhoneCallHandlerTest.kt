package com.twinmind.app.recording.handler

import android.content.Intent
import android.telephony.TelephonyManager
import io.mockk.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import android.os.Looper
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PhoneCallHandlerTest {

    private lateinit var handler  : PhoneCallHandler
    private lateinit var listener : PhoneCallHandler.Listener

    @Before
    fun setup() {
        handler  = PhoneCallHandler(RuntimeEnvironment.getApplication())
        listener = mockk(relaxed = true)
        handler.register(listener)
        // drain any setup broadcasts
        shadowOf(Looper.getMainLooper()).idle()
    }

    @Test
    fun `onCallStarted fires when state is RINGING`() {
        sendState(TelephonyManager.EXTRA_STATE_RINGING)
        verify(exactly = 1) { listener.onCallStarted() }
    }

    @Test
    fun `onCallStarted fires when state is OFFHOOK`() {
        sendState(TelephonyManager.EXTRA_STATE_OFFHOOK)
        verify(exactly = 1) { listener.onCallStarted() }
    }

    @Test
    fun `onCallEnded fires when state returns to IDLE`() {
        sendState(TelephonyManager.EXTRA_STATE_RINGING)
        sendState(TelephonyManager.EXTRA_STATE_IDLE)
        verify(exactly = 1) { listener.onCallEnded() }
    }

    @Test
    fun `onCallStarted not fired twice for consecutive ringing states`() {
        sendState(TelephonyManager.EXTRA_STATE_RINGING)
        sendState(TelephonyManager.EXTRA_STATE_OFFHOOK)
        verify(exactly = 1) { listener.onCallStarted() }
    }

    @Test
    fun `unregister stops events from firing`() {
        handler.unregister()
        shadowOf(Looper.getMainLooper()).idle()
        sendState(TelephonyManager.EXTRA_STATE_RINGING)
        verify(exactly = 0) { listener.onCallStarted() }
    }

    // ── helper — sends broadcast AND drains the looper ───────────────────────
    private fun sendState(state: String) {
        val intent = Intent(TelephonyManager.ACTION_PHONE_STATE_CHANGED)
            .putExtra(TelephonyManager.EXTRA_STATE, state)
        RuntimeEnvironment.getApplication().sendBroadcast(intent)
        shadowOf(Looper.getMainLooper()).idle()   // ← deliver the broadcast now
    }
}