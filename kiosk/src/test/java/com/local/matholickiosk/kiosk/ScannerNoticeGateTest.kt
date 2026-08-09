package com.local.matholickiosk.kiosk

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScannerNoticeGateTest {
    @Test
    fun currentNoticeSuppressesGuidanceUntilItFinishes() {
        val gate = ScannerNoticeGate()

        val generation = gate.begin()

        assertTrue(gate.suppressesPassiveGuidance())
        assertTrue(gate.finish(generation))
        assertFalse(gate.suppressesPassiveGuidance())
    }

    @Test
    fun staleTimerCannotReleaseANewerNotice() {
        val gate = ScannerNoticeGate()
        val staleGeneration = gate.begin()
        val currentGeneration = gate.begin()

        assertFalse(gate.finish(staleGeneration))
        assertTrue(gate.suppressesPassiveGuidance())
        assertTrue(gate.finish(currentGeneration))
        assertFalse(gate.suppressesPassiveGuidance())
    }

    @Test
    fun substantiveScannerActionInvalidatesTheNotice() {
        val gate = ScannerNoticeGate()
        val staleGeneration = gate.begin()

        gate.invalidate()

        assertFalse(gate.suppressesPassiveGuidance())
        assertFalse(gate.finish(staleGeneration))
    }
}
