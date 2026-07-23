package com.local.matholickiosk.kiosk.qr

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

object QrImageRenderer {
    fun render(payload: String, sizePixels: Int): Bitmap {
        require(sizePixels >= 256) { "QR image is too small" }
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
        for (y in 0 until sizePixels) {
            val rowOffset = y * sizePixels
            for (x in 0 until sizePixels) {
                pixels[rowOffset + x] = if (matrix[x, y]) Color.BLACK else Color.WHITE
            }
        }
        return Bitmap.createBitmap(sizePixels, sizePixels, Bitmap.Config.ARGB_8888).apply {
            setPixels(pixels, 0, sizePixels, 0, 0, sizePixels, sizePixels)
        }
    }
}
