package com.local.matholickiosk.kiosk

import com.local.matholickiosk.kiosk.qr.QrFrameQuality
import com.local.matholickiosk.kiosk.qr.QrFrameQualityClassifier
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
            QrFrameQuality.LOW_CONTRAST,
            QrFrameQualityClassifier.classify(IntArray(100) { 120 + it % 10 }),
        )
        assertNull(
            QrFrameQualityClassifier.classify(IntArray(100) { if (it % 2 == 0) 20 else 220 }),
        )
    }
}
