package com.local.matholickiosk.kiosk

import android.content.Context
import android.util.Log
import java.io.File
import java.io.RandomAccessFile
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
                purgeExpiredLocked()
                if (current.length() + line.toByteArray().size > MAX_BYTES) {
                    if (previous.exists() && !previous.delete()) return@runCatching
                    if (current.exists() && !current.renameTo(previous)) return@runCatching
                }
                current.appendText(line, Charsets.UTF_8)
            }
        }
    }

    fun dumpForAdb(nonce: String) {
        if (!isSafeDumpNonce(nonce)) return
        Log.i(ADB_DUMP_TAG, "BEGIN:$nonce")
        synchronized(lock) {
            purgeExpiredLocked()
            listOf(previous, current).forEach { file ->
                runCatching {
                    readBoundedTail(file)
                    .filter(::isSafeDumpLine)
                    .forEach { line -> Log.i(ADB_DUMP_TAG, "$nonce $line") }
                }
            }
        }
        Log.i(ADB_DUMP_TAG, "END:$nonce")
    }

    private fun purgeExpiredLocked() {
        val cutoff = nowEpochMs() - RETENTION_MS
        listOf(previous, current).forEach { file ->
            val modified = file.lastModified()
            if (modified > 0L && modified < cutoff) file.delete()
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
        private const val MAX_DUMP_BYTES_PER_FILE = MAX_BYTES + 4 * 1024
        private const val MAX_DUMP_LINES_PER_FILE = 200
        private const val RETENTION_MS = 90L * 24 * 60 * 60 * 1000
        internal const val ADB_DUMP_TAG = "KioskPrivateDiagnostics"
        private val DUMP_NONCE = Regex("[A-F0-9]{16,64}")
        private val DUMP_LINE =
            Regex("[0-9]{1,20}\\t[A-Z0-9_.-]{1,80}(\\t[A-Z0-9_.-]{1,80})?")

        internal fun isSafeDumpNonce(value: String): Boolean = DUMP_NONCE.matches(value)

        internal fun isSafeDumpLine(value: String): Boolean = DUMP_LINE.matches(value)

        internal fun readBoundedTail(file: File): List<String> {
            if (!file.isFile) return emptyList()
            return RandomAccessFile(file, "r").use { input ->
                val start = (input.length() - MAX_DUMP_BYTES_PER_FILE).coerceAtLeast(0)
                input.seek(start)
                val bytes = ByteArray((input.length() - start).toInt())
                try {
                    input.readFully(bytes)
                    var text = bytes.toString(Charsets.UTF_8)
                    if (start > 0) text = text.substringAfter('\n', "")
                    text.lineSequence()
                        .filter(String::isNotEmpty)
                        .toList()
                        .takeLast(MAX_DUMP_LINES_PER_FILE)
                } finally {
                    bytes.fill(0)
                }
            }
        }
    }
}
