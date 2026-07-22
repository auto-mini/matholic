package com.local.matholickiosk.probe

import android.content.Context
import android.net.Uri
import org.json.JSONObject
import java.io.File
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicInteger

class ReportStore(private val context: Context) {
    private val sequence = AtomicInteger()
    private val reportDirectory: File
        get() = File(context.filesDir, "redacted_reports").also { it.mkdirs() }

    fun save(report: JSONObject): File {
        val timestamp = FILE_TIME_FORMAT.format(Instant.now())
        val file = File(reportDirectory, "capture_${timestamp}_${sequence.incrementAndGet()}.json")
        file.writeText(report.toString(2), Charsets.UTF_8)
        return file
    }

    fun list(): List<File> =
        reportDirectory.listFiles { file -> file.isFile && file.extension == "json" }
            ?.sortedByDescending(File::lastModified)
            .orEmpty()

    fun latest(): File? = list().firstOrNull()

    fun export(file: File, destination: Uri) {
        require(file.parentFile?.canonicalFile == reportDirectory.canonicalFile) {
            "Report must be inside private report directory"
        }
        context.contentResolver.openOutputStream(destination, "wt").use { output ->
            requireNotNull(output) { "Unable to open export destination" }
            file.inputStream().use { input -> input.copyTo(output) }
        }
    }

    fun deleteAll(): Int {
        var deleted = 0
        list().forEach { file ->
            if (file.delete()) deleted += 1
        }
        return deleted
    }

    companion object {
        private val FILE_TIME_FORMAT = DateTimeFormatter
            .ofPattern("yyyyMMdd_HHmmss_SSS")
            .withZone(ZoneOffset.UTC)
    }
}
