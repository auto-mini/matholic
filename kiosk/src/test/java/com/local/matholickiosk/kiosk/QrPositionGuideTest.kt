package com.local.matholickiosk.kiosk

import com.local.matholickiosk.kiosk.qr.QrFrameBounds
import com.local.matholickiosk.kiosk.qr.QrFrameGuidance
import com.local.matholickiosk.kiosk.qr.QrPositionGuide
import org.junit.Assert.assertEquals
import org.junit.Test

class QrPositionGuideTest {
    @Test
    fun `centered readable QR is ready`() {
        assertEquals(
            QrFrameGuidance.CENTERED,
            QrPositionGuide.classify(
                QrFrameBounds(350, 250, 650, 550),
                imageWidth = 1_000,
                imageHeight = 800,
                mirrorHorizontally = false,
            ),
        )
    }

    @Test
    fun `off-center guidance tells the card which way to move`() {
        assertEquals(
            QrFrameGuidance.MOVE_RIGHT,
            QrPositionGuide.classify(
                QrFrameBounds(20, 300, 220, 500),
                imageWidth = 1_000,
                imageHeight = 800,
                mirrorHorizontally = false,
            ),
        )
        assertEquals(
            QrFrameGuidance.MOVE_LEFT,
            QrPositionGuide.classify(
                QrFrameBounds(780, 300, 980, 500),
                imageWidth = 1_000,
                imageHeight = 800,
                mirrorHorizontally = false,
            ),
        )
    }

    @Test
    fun `front camera guidance mirrors the horizontal direction`() {
        assertEquals(
            QrFrameGuidance.MOVE_LEFT,
            QrPositionGuide.classify(
                QrFrameBounds(20, 300, 220, 500),
                imageWidth = 1_000,
                imageHeight = 800,
                mirrorHorizontally = true,
            ),
        )
    }

    @Test
    fun `slight horizontal offset stays inside the wider acceptance band`() {
        assertEquals(
            QrFrameGuidance.CENTERED,
            QrPositionGuide.classify(
                QrFrameBounds(220, 280, 420, 520),
                imageWidth = 1_000,
                imageHeight = 800,
                mirrorHorizontally = false,
            ),
        )
    }

    @Test
    fun `centered QR reports distance before acceptance`() {
        assertEquals(
            QrFrameGuidance.MOVE_CLOSER,
            QrPositionGuide.classify(
                QrFrameBounds(460, 360, 540, 440),
                imageWidth = 1_000,
                imageHeight = 800,
                mirrorHorizontally = false,
            ),
        )
        assertEquals(
            QrFrameGuidance.MOVE_FARTHER,
            QrPositionGuide.classify(
                QrFrameBounds(100, 50, 900, 750),
                imageWidth = 1_000,
                imageHeight = 800,
                mirrorHorizontally = false,
            ),
        )
    }
}
