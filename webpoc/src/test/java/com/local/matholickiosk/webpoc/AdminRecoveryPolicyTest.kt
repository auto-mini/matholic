package com.local.matholickiosk.webpoc

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdminRecoveryPolicyTest {
    @Test
    fun `admin recovery accepts only exact same-signature kiosk caller`() {
        assertTrue(
            AdminRecoveryPolicy.isTrustedCaller(
                callerPackage = "com.local.matholickiosk.kiosk",
                expectedPackage = "com.local.matholickiosk.kiosk",
                signaturesMatch = true,
            ),
        )
        assertFalse(
            AdminRecoveryPolicy.isTrustedCaller(
                callerPackage = "com.example.untrusted",
                expectedPackage = "com.local.matholickiosk.kiosk",
                signaturesMatch = true,
            ),
        )
        assertFalse(
            AdminRecoveryPolicy.isTrustedCaller(
                callerPackage = "com.local.matholickiosk.kiosk",
                expectedPackage = "com.local.matholickiosk.kiosk",
                signaturesMatch = false,
            ),
        )
        assertFalse(
            AdminRecoveryPolicy.isTrustedCaller(
                callerPackage = null,
                expectedPackage = "com.local.matholickiosk.kiosk",
                signaturesMatch = true,
            ),
        )
    }

    @Test
    fun `admin recovery sanitizes every persisted state except idle`() {
        WebPocState.entries.forEach { state ->
            if (state == WebPocState.IDLE) {
                assertFalse(AdminRecoveryPolicy.requiresSanitization(state))
            } else {
                assertTrue(AdminRecoveryPolicy.requiresSanitization(state))
            }
        }
    }
}
