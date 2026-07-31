package com.local.matholickiosk.webpoc

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class KioskRemoteSupportReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_SET_WEB_REMOTE_SUPPORT) return
        applyRemoteSupportIntent(context, intent)
    }

    companion object {
        const val ACTION_SET_WEB_REMOTE_SUPPORT =
            "com.local.matholickiosk.action.SET_WEB_REMOTE_SUPPORT"
    }
}
