package com.local.matholickiosk.kiosk

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.local.matholickiosk.kiosk.qr.QrImageRenderer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QrImageRendererInstrumentedTest {
    @Test
    fun rendererProducesRequestedBitmapWithinBoundedSize() {
        val bitmap = QrImageRenderer.render("KIOSK-QR-TEST", 256)
        try {
            assertEquals(256, bitmap.width)
            assertEquals(256, bitmap.height)
        } finally {
            bitmap.eraseColor(0)
            bitmap.recycle()
        }
    }

    @Test
    fun rendererRejectsUnboundedAllocation() {
        assertTrue(
            runCatching {
                QrImageRenderer.render(
                    "KIOSK-QR-TEST",
                    QrImageRenderer.MAX_SIZE_PIXELS + 1,
                )
            }.isFailure,
        )
    }
}
