package com.local.matholickiosk.kiosk

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
}
