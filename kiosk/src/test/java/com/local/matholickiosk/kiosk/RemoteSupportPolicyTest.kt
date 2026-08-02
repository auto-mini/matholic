package com.local.matholickiosk.kiosk

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RemoteSupportPolicyTest {
    @Test
    fun durationIsClampedToOneMinuteAndTwoHours() {
        assertEquals(
            61_000L,
            RemoteSupportPolicy.expiresAt(
                nowEpochMillis = 1_000L,
                requestedDurationMillis = 1L,
            ),
        )
        assertEquals(
            7_201_000L,
            RemoteSupportPolicy.expiresAt(
                nowEpochMillis = 1_000L,
                requestedDurationMillis = Long.MAX_VALUE,
            ),
        )
    }

    @Test
    fun supportIsActiveOnlyBeforeExpiryAndWithinTheSameBoot() {
        assertTrue(
            RemoteSupportPolicy.isActive(
                nowEpochMillis = 10_000L,
                currentBootCount = 7,
                storedBootCount = 7,
                expiresAtEpochMillis = 11_000L,
            ),
        )
        assertFalse(
            RemoteSupportPolicy.isActive(
                nowEpochMillis = 11_000L,
                currentBootCount = 7,
                storedBootCount = 7,
                expiresAtEpochMillis = 11_000L,
            ),
        )
        assertFalse(
            RemoteSupportPolicy.isActive(
                nowEpochMillis = 10_000L,
                currentBootCount = 8,
                storedBootCount = 7,
                expiresAtEpochMillis = 11_000L,
            ),
        )
    }

    @Test
    fun captureIsAlwaysBlockedOnSensitiveScreens() {
        assertFalse(RemoteSupportPolicy.canCapture(supportActive = true, sensitiveScreen = true))
        assertTrue(RemoteSupportPolicy.canCapture(supportActive = true, sensitiveScreen = false))
        assertFalse(RemoteSupportPolicy.canCapture(supportActive = false, sensitiveScreen = false))
    }
}
