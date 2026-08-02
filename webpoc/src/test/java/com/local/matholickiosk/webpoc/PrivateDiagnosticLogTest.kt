package com.local.matholickiosk.webpoc

import java.io.File
import java.nio.file.Files
import org.junit.Assert.assertEquals
import org.junit.Test

class PrivateDiagnosticLogTest {
    @Test
    fun `adb dump tail reader returns only the newest bounded lines`() {
        val directory = Files.createTempDirectory("web-private-log").toFile()
        val file = File(directory, "events.log")
        try {
            file.writeText((1..250).joinToString("\n") { "2026-08-03T00:00:00Z EVENT_$it" } + "\n")

            val lines = PrivateDiagnosticLog.readBoundedTail(file)

            assertEquals(200, lines.size)
            assertEquals("2026-08-03T00:00:00Z EVENT_51", lines.first())
            assertEquals("2026-08-03T00:00:00Z EVENT_250", lines.last())
        } finally {
            file.delete()
            directory.delete()
        }
    }
}
