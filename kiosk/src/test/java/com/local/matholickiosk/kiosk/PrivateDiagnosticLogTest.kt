package com.local.matholickiosk.kiosk

import java.io.File
import java.nio.file.Files
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PrivateDiagnosticLogTest {
    @Test
    fun `adb dump accepts only generated nonce and structural event lines`() {
        assertTrue(PrivateDiagnosticLog.isSafeDumpNonce("0123456789ABCDEF"))
        assertFalse(PrivateDiagnosticLog.isSafeDumpNonce("student-name"))

        assertTrue(
            PrivateDiagnosticLog.isSafeDumpLine(
                "1785456000000\tWEB_RETURN_FAILED\tRESULT_INCOMPLETE",
            ),
        )
        assertFalse(
            PrivateDiagnosticLog.isSafeDumpLine(
                "1785456000000\tLOGIN\tstudent@example.com",
            ),
        )
    }

    @Test
    fun `adb dump tail reader bounds output to the newest structural lines`() {
        val directory = Files.createTempDirectory("kiosk-private-log").toFile()
        val file = File(directory, "events.log")
        try {
            file.writeText((1..250).joinToString("\n") { "$it\tEVENT_$it" } + "\n")

            val lines = PrivateDiagnosticLog.readBoundedTail(file)

            assertEquals(200, lines.size)
            assertEquals("51\tEVENT_51", lines.first())
            assertEquals("250\tEVENT_250", lines.last())
        } finally {
            file.delete()
            directory.delete()
        }
    }
}
