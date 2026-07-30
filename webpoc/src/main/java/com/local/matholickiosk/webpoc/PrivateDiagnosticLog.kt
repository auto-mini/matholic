package com.local.matholickiosk.webpoc

import android.content.Context
import java.io.File
import java.time.Instant

internal object PrivateDiagnosticLog {
    private const val FILE_NAME = "web-events.log"
    private const val PREVIOUS_FILE_NAME = "web-events.previous.log"
    private const val MAX_BYTES = 256 * 1024L
    private val safeCode = Regex("[^A-Z0-9_.:-]")

    @Synchronized
    fun event(context: Context, code: String) {
        runCatching {
            val current = File(context.filesDir, FILE_NAME)
            if (current.length() >= MAX_BYTES) {
                val previous = File(context.filesDir, PREVIOUS_FILE_NAME)
                if (previous.exists()) previous.delete()
                current.renameTo(previous)
            }
            val normalized = code.uppercase()
                .replace(safeCode, "_")
                .take(80)
                .ifBlank { "UNKNOWN" }
            current.appendText("${Instant.now()} $normalized\n")
        }
    }
}
