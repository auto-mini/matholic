package com.local.matholickiosk.kiosk

import com.local.matholickiosk.kiosk.qr.QrFrameQuality
import com.local.matholickiosk.kiosk.qr.QrFrameQualityClassifier
import com.local.matholickiosk.kiosk.qr.QrFrameQualityStabilizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class QrFrameQualityClassifierTest {
    @Test
    fun `classifies dark glare and low contrast frames`() {
        assertEquals(
            QrFrameQuality.TOO_DARK,
            QrFrameQualityClassifier.classify(IntArray(100) { 20 }),
        )
        assertEquals(
            QrFrameQuality.GLARE,
            QrFrameQualityClassifier.classify(
                IntArray(100) { if (it < 30) 255 else 100 },
            ),
        )
        assertEquals(
            QrFrameQuality.GLARE,
            QrFrameQualityClassifier.classify(
                IntArray(100) { if (it < 10) 255 else 100 },
            ),
        )
        assertEquals(
            QrFrameQuality.LOW_CONTRAST,
            QrFrameQualityClassifier.classify(IntArray(100) { 120 + it % 10 }),
        )
        assertNull(
            QrFrameQualityClassifier.classify(IntArray(100) { if (it % 2 == 0) 20 else 220 }),
        )
    }

    @Test
    fun `requires stable quality and resets transient frame changes`() {
        val stabilizer = QrFrameQualityStabilizer(requiredConsecutiveFrames = 3)

        assertNull(stabilizer.accept(QrFrameQuality.TOO_DARK))
        assertNull(stabilizer.accept(QrFrameQuality.LOW_CONTRAST))
        assertNull(stabilizer.accept(QrFrameQuality.GLARE))
        assertNull(stabilizer.accept(null))

        assertNull(stabilizer.accept(QrFrameQuality.GLARE))
        assertNull(stabilizer.accept(QrFrameQuality.GLARE))
        assertEquals(QrFrameQuality.GLARE, stabilizer.accept(QrFrameQuality.GLARE))
        assertEquals(QrFrameQuality.GLARE, stabilizer.accept(QrFrameQuality.GLARE))

        assertNull(stabilizer.accept(QrFrameQuality.TOO_DARK))
        assertNull(stabilizer.accept(QrFrameQuality.TOO_DARK))
        assertEquals(QrFrameQuality.TOO_DARK, stabilizer.accept(QrFrameQuality.TOO_DARK))
    }
}
