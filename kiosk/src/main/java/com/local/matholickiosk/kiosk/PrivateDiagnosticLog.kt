package com.local.matholickiosk.kiosk

import android.content.Context
import android.util.Log
import java.io.File
import java.util.Locale

class PrivateDiagnosticLog(
    context: Context,
    private val nowEpochMs: () -> Long = System::currentTimeMillis,
) {
    private val directory = File(context.filesDir, "diagnostics").apply { mkdirs() }
    private val current = File(directory, "kiosk-events.log")
    private val previous = File(directory, "kiosk-events.previous.log")
    private val lock = Any()

    fun record(event: String, reason: String? = null) {
        val safeEvent = event.safeCode() ?: return
        val safeReason = reason?.safeCode()
        val line = buildString {
            append(nowEpochMs())
            append('\t')
            append(safeEvent)
            safeReason?.let {
                append('\t')
                append(it)
            }
            append('\n')
        }
        synchronized(lock) {
            runCatching {
                if (current.length() + line.toByteArray().size > MAX_BYTES) {
                    previous.delete()
                    current.renameTo(previous)
                }
                current.appendText(line, Charsets.UTF_8)
            }
        }
    }

    fun dumpForAdb(nonce: String) {
        if (!isSafeDumpNonce(nonce)) return
        Log.i(ADB_DUMP_TAG, "BEGIN:$nonce")
        listOf(previous, current).forEach { file ->
            runCatching {
                file.readLines(Charsets.UTF_8)
                    .takeLast(MAX_DUMP_LINES_PER_FILE)
                    .filter(::isSafeDumpLine)
                    .forEach { line -> Log.i(ADB_DUMP_TAG, "$nonce $line") }
            }
        }
        Log.i(ADB_DUMP_TAG, "END:$nonce")
    }

    private fun String.safeCode(): String? {
        val normalized = trim().uppercase(Locale.ROOT).take(80)
        return normalized.takeIf {
            it.isNotEmpty() && it.all { character ->
                character in 'A'..'Z' ||
                    character in '0'..'9' ||
                    character == '_' ||
                    character == '-' ||
                    character == '.'
            }
        }
    }

    companion object {
        private const val MAX_BYTES = 256 * 1024
        private const val MAX_DUMP_LINES_PER_FILE = 200
        internal const val ADB_DUMP_TAG = "KioskPrivateDiagnostics"
        private val DUMP_NONCE = Regex("[A-F0-9]{16,64}")
        private val DUMP_LINE =
            Regex("[0-9]{1,20}\\t[A-Z0-9_.-]{1,80}(\\t[A-Z0-9_.-]{1,80})?")

        internal fun isSafeDumpNonce(value: String): Boolean = DUMP_NONCE.matches(value)

        internal fun isSafeDumpLine(value: String): Boolean = DUMP_LINE.matches(value)
    }
}
