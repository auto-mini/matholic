package com.local.matholickiosk.kiosk

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AdbRemoteSupportReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_SET_REMOTE_SUPPORT) return
        val store = RemoteSupportStore(context)
        if (intent.getBooleanExtra(EXTRA_ENABLED, false)) {
            val seconds = intent
                .getIntExtra(EXTRA_DURATION_SECONDS, DEFAULT_DURATION_SECONDS)
                .toLong()
            store.enable(seconds * 1_000L)
        } else {
            store.disable()
        }
    }

    companion object {
        const val ACTION_SET_REMOTE_SUPPORT =
            "com.local.matholickiosk.kiosk.action.SET_REMOTE_SUPPORT"
        const val EXTRA_ENABLED = "enabled"
        const val EXTRA_DURATION_SECONDS = "duration_seconds"
        const val DEFAULT_DURATION_SECONDS = 30 * 60
    }
}
