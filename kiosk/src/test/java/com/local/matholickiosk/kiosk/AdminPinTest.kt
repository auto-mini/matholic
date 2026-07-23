package com.local.matholickiosk.kiosk

import com.local.matholickiosk.kiosk.security.AdminLockoutPolicy
import com.local.matholickiosk.kiosk.security.AdminPin
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdminPinTest {
    @Test
    fun pinRequiresSixToTwelveDigits() {
        assertFalse(AdminPin.isValidFormat("12345".toCharArray()))
        assertFalse(AdminPin.isValidFormat("12345a".toCharArray()))
        assertFalse(AdminPin.isValidFormat("1234567890123".toCharArray()))
        assertTrue(AdminPin.isValidFormat("123456".toCharArray()))
        assertTrue(AdminPin.isValidFormat("123456789012".toCharArray()))
    }

    @Test
    fun pbkdf2VerifierAcceptsOnlyMatchingPin() {
        val verifier = AdminPin.create("654321".toCharArray(), iterations = 100_000)

        assertTrue(AdminPin.verify("654321".toCharArray(), verifier))
        assertFalse(AdminPin.verify("654322".toCharArray(), verifier))
    }

    @Test
    fun lockoutDelayIsExponentialAndCapped() {
        assertTrue(AdminLockoutPolicy.delayMillis(1) == 1_000L)
        assertTrue(AdminLockoutPolicy.delayMillis(2) == 2_000L)
        assertTrue(AdminLockoutPolicy.delayMillis(9) == 256_000L)
        assertTrue(AdminLockoutPolicy.delayMillis(10) == 300_000L)
    }
}
