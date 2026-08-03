package com.local.matholickiosk.webpoc

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AdbRemoteSupportReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_SET_REMOTE_SUPPORT) return
        resultCode = if (applyRemoteSupportIntent(context, intent)) {
            android.app.Activity.RESULT_OK
        } else {
            android.app.Activity.RESULT_CANCELED
        }
    }

    companion object {
        const val ACTION_SET_REMOTE_SUPPORT =
            "com.local.matholickiosk.webpoc.action.SET_REMOTE_SUPPORT"
    }
}
