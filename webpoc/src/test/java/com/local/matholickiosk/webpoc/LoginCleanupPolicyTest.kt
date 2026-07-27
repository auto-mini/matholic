package com.local.matholickiosk.webpoc

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LoginCleanupPolicyTest {
    @Test
    fun `cleanup canonicalizes official login error redirect before fingerprint`() {
        val errorRedirect =
            "https://login.matholic.com/?error=true&url=https%3A%2F%2Fim.matholic.com%2Fcourse"

        assertTrue(
            LoginCleanupPolicy.shouldCanonicalizeBeforeFingerprint(
                WebPocState.RECOVERY_REQUIRED,
                errorRedirect,
            ),
        )
        assertTrue(
            LoginCleanupPolicy.shouldCanonicalizeBeforeFingerprint(
                WebPocState.LOGOUT_NAVIGATE,
                errorRedirect,
            ),
        )
    }

    @Test
    fun `cleanup never rewrites canonical login or non-cleanup navigation`() {
        val errorRedirect =
            "https://login.matholic.com/?error=true&url=https%3A%2F%2Fim.matholic.com%2Fcourse"

        assertFalse(
            LoginCleanupPolicy.shouldCanonicalizeBeforeFingerprint(
                WebPocState.RECOVERY_REQUIRED,
                WebSecurityPolicy.LOGIN_URL,
            ),
        )
        assertFalse(
            LoginCleanupPolicy.shouldCanonicalizeBeforeFingerprint(
                WebPocState.LOGIN_SUBMIT,
                errorRedirect,
            ),
        )
        assertFalse(
            LoginCleanupPolicy.shouldCanonicalizeBeforeFingerprint(
                WebPocState.RECOVERY_REQUIRED,
                "https://example.invalid/?error=true",
            ),
        )
    }
}
