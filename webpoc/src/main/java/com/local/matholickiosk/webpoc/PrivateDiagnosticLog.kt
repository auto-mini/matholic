package com.local.matholickiosk.webpoc

import android.content.Context
import android.util.Log
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

    fun dumpForAdb(context: Context, nonce: String) {
        if (!isSafeDumpNonce(nonce)) return
        Log.i(ADB_DUMP_TAG, "BEGIN:$nonce")
        listOf(PREVIOUS_FILE_NAME, FILE_NAME).forEach { name ->
            runCatching {
                File(context.filesDir, name)
                    .readLines(Charsets.UTF_8)
                    .takeLast(MAX_DUMP_LINES_PER_FILE)
                    .filter(::isSafeDumpLine)
                    .forEach { line -> Log.i(ADB_DUMP_TAG, "$nonce $line") }
            }
        }
        Log.i(ADB_DUMP_TAG, "END:$nonce")
    }

    internal fun isSafeDumpNonce(value: String): Boolean = DUMP_NONCE.matches(value)

    internal fun isSafeDumpLine(value: String): Boolean = DUMP_LINE.matches(value)

    internal const val ADB_DUMP_TAG = "LearningPrivateDiagnostics"
    private const val MAX_DUMP_LINES_PER_FILE = 200
    private val DUMP_NONCE = Regex("[A-F0-9]{16,64}")
    private val DUMP_LINE =
        Regex("[0-9]{4}-[0-9:.+TZ-]{15,35} [A-Z0-9_.:-]{1,80}")
}
