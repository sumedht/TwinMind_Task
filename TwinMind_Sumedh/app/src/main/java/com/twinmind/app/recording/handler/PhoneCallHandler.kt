package com.twinmind.app.recording.handler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.telephony.TelephonyManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class PhoneCallHandler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    interface Listener {
        fun onCallStarted()
        fun onCallEnded()
    }

    private var listener: Listener? = null
    private var inCall = false

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context, intent: Intent) {
            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            when (state) {
                TelephonyManager.EXTRA_STATE_RINGING,
                TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                    if (!inCall) {
                        inCall = true
                        listener?.onCallStarted()
                    }
                }
                TelephonyManager.EXTRA_STATE_IDLE -> {
                    if (inCall) {
                        inCall = false
                        listener?.onCallEnded()
                    }
                }
            }
        }
    }

    fun register(l: Listener) {
        listener = l
        context.registerReceiver(receiver, IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED))
    }

    fun unregister() {
        runCatching { context.unregisterReceiver(receiver) }
        listener = null
    }
}