package com.local.matholickiosk.webpoc

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Gate3RunSessionTest {
    @Test
    fun `alternates A and B and counts only completed cycles`() {
        val session = Gate3RunSession(account("A"), account("B"), targetCycles = 4)

        val first = session.nextAttempt()
        assertEquals("A", first.slotLabel)
        assertEquals("name-A", first.expectedDisplayName)
        assertEquals(0, session.completedCycles)
        first.credentials.wipe()

        session.recordCompletedCycle()
        val second = session.nextAttempt()
        assertEquals("B", second.slotLabel)
        assertEquals("name-B", second.expectedDisplayName)
        second.credentials.wipe()

        session.recordCompletedCycle()
        assertEquals("A", session.nextSlotLabel)
        assertFalse(session.isComplete)

        session.recordCompletedCycle()
        session.recordCompletedCycle()
        assertTrue(session.isComplete)
        assertEquals(4, session.completedCycles)
        session.wipe()
    }

    @Test(expected = IllegalStateException::class)
    fun `wiped session cannot produce another attempt`() {
        val session = Gate3RunSession(account("A"), account("B"), targetCycles = 2)
        session.wipe()
        session.nextAttempt()
    }

    @Test
    fun `failed attempt does not advance count or switch account`() {
        val session = Gate3RunSession(account("A"), account("B"), targetCycles = 2)

        val failedAttempt = session.nextAttempt()
        assertEquals("A", failedAttempt.slotLabel)
        failedAttempt.credentials.wipe()

        assertEquals(0, session.completedCycles)
        assertEquals("A", session.nextSlotLabel)

        val nextAttempt = session.nextAttempt()
        assertEquals("A", nextAttempt.slotLabel)
        nextAttempt.credentials.wipe()
        session.wipe()
    }

    @Test
    fun `account input string representation does not expose values`() {
        val input = account("secret-slot")
        val rendered = input.toString()
        assertFalse(rendered.contains(input.expectedDisplayName))
        assertFalse(rendered.contains(input.username))
        assertFalse(rendered.contains(input.password))
    }

    private fun account(slot: String) = Gate3AccountInput(
        expectedDisplayName = "name-$slot",
        username = "user-$slot",
        password = "pass-$slot",
    )
}
