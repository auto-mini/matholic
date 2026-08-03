package com.local.matholickiosk.webpoc

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AdbDiagnosticDumpReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_DUMP_DIAGNOSTICS) return
        val nonce = intent.getStringExtra(EXTRA_NONCE) ?: return
        PrivateDiagnosticLog.dumpForAdb(context, nonce)
    }

    companion object {
        const val ACTION_DUMP_DIAGNOSTICS =
            "com.local.matholickiosk.webpoc.action.DUMP_PRIVATE_DIAGNOSTICS"
        const val EXTRA_NONCE = "nonce"
    }
}
