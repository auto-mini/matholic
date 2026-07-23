package com.local.matholickiosk.kiosk

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PrintAttributes
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.local.matholickiosk.kiosk.print.QrPrintDocumentAdapter
import com.local.matholickiosk.kiosk.print.QrPrintPdfWriter
import com.local.matholickiosk.kiosk.qr.QrImageRenderer
import com.local.matholickiosk.kiosk.qr.QrTokenCodec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class QrPrintDocumentAdapterInstrumentedTest {
    @Test
    fun writesOnePagePdfAndWipesOwnedBitmapOnFinish() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val output = File(context.cacheDir, "synthetic-qr-print-${System.nanoTime()}.pdf")
        val qrBitmap = QrImageRenderer.render(
            QrTokenCodec().issue().payload,
            720,
        ).copy(Bitmap.Config.ARGB_8888, true)
        val adapter = QrPrintDocumentAdapter(
            context,
            "가상학생-*",
            qrBitmap,
        )
        val attributes = PrintAttributes.Builder()
            .setMediaSize(PrintAttributes.MediaSize.ISO_A4.asPortrait())
            .setResolution(PrintAttributes.Resolution("test", "test", 300, 300))
            .setMinMargins(PrintAttributes.Margins(500, 500, 500, 500))
            .setColorMode(PrintAttributes.COLOR_MODE_MONOCHROME)
            .build()

        try {
            ParcelFileDescriptor.open(
                output,
                ParcelFileDescriptor.MODE_CREATE or
                    ParcelFileDescriptor.MODE_TRUNCATE or
                    ParcelFileDescriptor.MODE_READ_WRITE,
            ).use { destination ->
                assertTrue(
                    QrPrintPdfWriter.write(
                        context = context,
                        attributes = attributes,
                        maskedDisplayName = "가상학생-*",
                        qrBitmap = qrBitmap,
                        destination = destination.fileDescriptor,
                        cancellationSignal = CancellationSignal(),
                    ),
                )
            }
            assertTrue(output.length() > 1_024)

            ParcelFileDescriptor.open(
                output,
                ParcelFileDescriptor.MODE_READ_ONLY,
            ).use { source ->
                PdfRenderer(source).use { renderer ->
                    assertEquals(1, renderer.pageCount)
                    renderer.openPage(0).use { page ->
                        val preview = Bitmap.createBitmap(
                            page.width,
                            page.height,
                            Bitmap.Config.ARGB_8888,
                        )
                        try {
                            preview.eraseColor(Color.WHITE)
                            page.render(
                                preview,
                                null,
                                null,
                                PdfRenderer.Page.RENDER_MODE_FOR_PRINT,
                            )
                            var darkSamples = 0
                            for (y in 0 until preview.height step 8) {
                                for (x in 0 until preview.width step 8) {
                                    val pixel = preview.getPixel(x, y)
                                    if (
                                        Color.red(pixel) < 128 &&
                                        Color.green(pixel) < 128 &&
                                        Color.blue(pixel) < 128
                                    ) {
                                        darkSamples += 1
                                    }
                                }
                            }
                            assertTrue(darkSamples > 100)
                        } finally {
                            preview.recycle()
                        }
                    }
                }
            }
        } finally {
            adapter.onFinish()
            assertTrue(qrBitmap.isRecycled)
            output.delete()
        }
    }
}
