package com.local.matholickiosk.webpoc

import android.content.Context
import android.util.Log
import java.io.File
import java.io.RandomAccessFile
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
            purgeExpired(context)
            if (current.length() >= MAX_BYTES) {
                val previous = File(context.filesDir, PREVIOUS_FILE_NAME)
                if (previous.exists() && !previous.delete()) return@runCatching
                if (current.exists() && !current.renameTo(previous)) return@runCatching
            }
            val normalized = code.uppercase()
                .replace(safeCode, "_")
                .take(80)
                .ifBlank { "UNKNOWN" }
            current.appendText("${Instant.now()} $normalized\n")
        }
    }

    @Synchronized
    fun dumpForAdb(context: Context, nonce: String) {
        if (!isSafeDumpNonce(nonce)) return
        Log.i(ADB_DUMP_TAG, "BEGIN:$nonce")
        purgeExpired(context)
        listOf(PREVIOUS_FILE_NAME, FILE_NAME).forEach { name ->
            runCatching {
                readBoundedTail(File(context.filesDir, name))
                    .filter(::isSafeDumpLine)
                    .forEach { line -> Log.i(ADB_DUMP_TAG, "$nonce $line") }
            }
        }
        Log.i(ADB_DUMP_TAG, "END:$nonce")
    }

    private fun purgeExpired(context: Context) {
        val cutoff = System.currentTimeMillis() - RETENTION_MS
        listOf(PREVIOUS_FILE_NAME, FILE_NAME).forEach { name ->
            val file = File(context.filesDir, name)
            val modified = file.lastModified()
            if (modified > 0L && modified < cutoff) file.delete()
        }
    }

    internal fun isSafeDumpNonce(value: String): Boolean = DUMP_NONCE.matches(value)

    internal fun isSafeDumpLine(value: String): Boolean = DUMP_LINE.matches(value)

    internal const val ADB_DUMP_TAG = "LearningPrivateDiagnostics"
    private const val MAX_DUMP_BYTES_PER_FILE = MAX_BYTES + 4 * 1024
    private const val MAX_DUMP_LINES_PER_FILE = 200
    private const val RETENTION_MS = 90L * 24 * 60 * 60 * 1000
    private val DUMP_NONCE = Regex("[A-F0-9]{16,64}")
    private val DUMP_LINE =
        Regex("[0-9]{4}-[0-9:.+TZ-]{15,35} [A-Z0-9_.:-]{1,80}")

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
