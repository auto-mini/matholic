package com.local.matholickiosk.kiosk

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AdbRemoteSupportReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val store = RemoteSupportStore(context)
        when (intent.action) {
            ACTION_SET_REMOTE_SUPPORT -> {
                val stored = runCatching { if (intent.getBooleanExtra(EXTRA_ENABLED, false)) {
                    val seconds = intent
                        .getIntExtra(EXTRA_DURATION_SECONDS, DEFAULT_DURATION_SECONDS)
                        .toLong()
                    store.enable(seconds * 1_000L)
                } else {
                    store.disable()
                } }.isSuccess
                resultCode = if (stored) {
                    android.app.Activity.RESULT_OK
                } else {
                    android.app.Activity.RESULT_CANCELED
                }
            }
            ACTION_TEST_QR_HASH -> {
                val tokenHash = RemoteQrTestPayload.decode(
                    encodedHash = intent.getStringExtra(EXTRA_TOKEN_HASH_BASE64),
                    supportActive = store.activeUntilEpochMillis() != null,
                ) ?: return
                if (!RemoteQrTestBridge.deliver(tokenHash)) {
                    tokenHash.fill(0)
                }
            }
        }
    }

    companion object {
        const val ACTION_SET_REMOTE_SUPPORT =
            "com.local.matholickiosk.kiosk.action.SET_REMOTE_SUPPORT"
        const val ACTION_TEST_QR_HASH =
            "com.local.matholickiosk.kiosk.action.TEST_QR_HASH"
        const val EXTRA_ENABLED = "enabled"
        const val EXTRA_DURATION_SECONDS = "duration_seconds"
        const val EXTRA_TOKEN_HASH_BASE64 = "token_hash_base64"
        const val DEFAULT_DURATION_SECONDS = 30 * 60
    }
}
