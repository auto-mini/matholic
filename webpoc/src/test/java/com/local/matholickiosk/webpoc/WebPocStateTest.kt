package com.local.matholickiosk.webpoc

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WebPocStateTest {
    @Test
    fun `sensitive states require recovery after process restart`() {
        assertTrue(WebPocState.LOGIN_FILL.requiresRecoveryAfterRestart())
        assertTrue(WebPocState.LOGIN_SUBMIT.requiresRecoveryAfterRestart())
        assertTrue(WebPocState.LOGIN_VERIFY.requiresRecoveryAfterRestart())
        assertTrue(WebPocState.ACTIVE.requiresRecoveryAfterRestart())
        assertTrue(WebPocState.LOGOUT_SUBMIT.requiresRecoveryAfterRestart())
    }

    @Test
    fun `safe and explicit lock states do not silently resume automation`() {
        assertFalse(WebPocState.IDLE.requiresRecoveryAfterRestart())
        assertFalse(WebPocState.PREFLIGHT.requiresRecoveryAfterRestart())
        assertFalse(WebPocState.SESSION_SANITIZE.requiresRecoveryAfterRestart())
        assertFalse(WebPocState.LOCKED.requiresRecoveryAfterRestart())
        assertFalse(WebPocState.MAINTENANCE_REQUIRED.requiresRecoveryAfterRestart())
    }
}
