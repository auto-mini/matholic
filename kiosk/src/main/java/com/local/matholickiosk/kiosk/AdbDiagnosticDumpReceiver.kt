package com.local.matholickiosk.kiosk

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AdbDiagnosticDumpReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_DUMP_DIAGNOSTICS) return
        val nonce = intent.getStringExtra(EXTRA_NONCE) ?: return
        PrivateDiagnosticLog(context).dumpForAdb(nonce)
    }

    companion object {
        const val ACTION_DUMP_DIAGNOSTICS =
            "com.local.matholickiosk.kiosk.action.DUMP_PRIVATE_DIAGNOSTICS"
        const val EXTRA_NONCE = "nonce"
    }
}
