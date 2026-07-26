package com.local.matholickiosk.kiosk

import com.local.matholickiosk.kiosk.qr.QrDecisionDeliveryGate
import com.local.matholickiosk.kiosk.qr.QrFrameDecision
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class QrDecisionDeliveryGateTest {
    @Test
    fun currentFrameIsDeliveredWhileScanningRemainsEnabled() {
        val gate = QrDecisionDeliveryGate()
        val generation = requireNotNull(gate.currentFrameGeneration())
        val hash = ByteArray(32) { 0x5a }
        var delivered = false

        assertTrue(
            gate.deliverIfCurrent(
                generation,
                QrFrameDecision.Accept(hash),
            ) {
                delivered = true
            },
        )
        assertTrue(delivered)
        assertTrue(hash.any { it != 0.toByte() })
    }

    @Test
    fun resultFromBeforeAdminPauseIsDiscardedAndWipedAfterResume() {
        val gate = QrDecisionDeliveryGate()
        val staleGeneration = requireNotNull(gate.currentFrameGeneration())
        val hash = ByteArray(32) { 0x5a }
        var delivered = false

        gate.setEnabled(false)
        gate.setEnabled(true)

        assertFalse(
            gate.deliverIfCurrent(
                staleGeneration,
                QrFrameDecision.Accept(hash),
            ) {
                delivered = true
            },
        )
        assertFalse(delivered)
        assertTrue(hash.all { it == 0.toByte() })
    }

    @Test
    fun disabledScannerDoesNotStartAFrameGeneration() {
        val gate = QrDecisionDeliveryGate()

        gate.setEnabled(false)

        assertNull(gate.currentFrameGeneration())
        assertFalse(gate.isEnabled())
    }
}
