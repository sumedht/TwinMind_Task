package com.twinmind.app.recording.handler

import android.bluetooth.BluetoothHeadset
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class HeadsetHandler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    interface Listener {
        fun onHeadsetSourceChanged(description: String)
    }

    private var listener: Listener? = null

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_HEADSET_PLUG -> {
                    val state = intent.getIntExtra("state", -1)
                    val name = intent.getStringExtra("name") ?: "Wired headset"
                    if (state == 1) listener?.onHeadsetSourceChanged("Wired headset connected: $name")
                    else if (state == 0) listener?.onHeadsetSourceChanged("Wired headset disconnected")
                }
                BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED -> {
                    val state = intent.getIntExtra(
                        BluetoothHeadset.EXTRA_STATE,
                        BluetoothHeadset.STATE_DISCONNECTED
                    )
                    if (state == BluetoothHeadset.STATE_CONNECTED)
                        listener?.onHeadsetSourceChanged("Bluetooth headset connected")
                    else if (state == BluetoothHeadset.STATE_DISCONNECTED)
                        listener?.onHeadsetSourceChanged("Bluetooth headset disconnected")
                }
            }
        }
    }

    fun register(l: Listener) {
        listener = l
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_HEADSET_PLUG)
            addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED)
        }
        context.registerReceiver(receiver, filter)
    }

    fun unregister() {
        runCatching { context.unregisterReceiver(receiver) }
        listener = null
    }
}