package com.local.matholickiosk.kiosk.qr

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

object QrImageRenderer {
    fun render(payload: String, sizePixels: Int): Bitmap {
        require(sizePixels in MIN_SIZE_PIXELS..MAX_SIZE_PIXELS) {
            "QR image size must be between $MIN_SIZE_PIXELS and $MAX_SIZE_PIXELS pixels"
        }
        val matrix = MultiFormatWriter().encode(
            payload,
            BarcodeFormat.QR_CODE,
            sizePixels,
            sizePixels,
            mapOf(
                EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.H,
                EncodeHintType.MARGIN to 2,
                EncodeHintType.CHARACTER_SET to "UTF-8",
            ),
        )
        val pixels = IntArray(sizePixels * sizePixels)
        return try {
            for (y in 0 until sizePixels) {
                val rowOffset = y * sizePixels
                for (x in 0 until sizePixels) {
                    pixels[rowOffset + x] = if (matrix[x, y]) Color.BLACK else Color.WHITE
                }
            }
            Bitmap.createBitmap(sizePixels, sizePixels, Bitmap.Config.ARGB_8888).apply {
                setPixels(pixels, 0, sizePixels, 0, 0, sizePixels, sizePixels)
            }
        } finally {
            pixels.fill(0)
            matrix.clear()
        }
    }

    internal const val MIN_SIZE_PIXELS = 256
    internal const val MAX_SIZE_PIXELS = 2_048
}
