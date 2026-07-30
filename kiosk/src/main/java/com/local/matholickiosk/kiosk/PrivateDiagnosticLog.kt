package com.local.matholickiosk.kiosk

import android.content.Context
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
    }
}
