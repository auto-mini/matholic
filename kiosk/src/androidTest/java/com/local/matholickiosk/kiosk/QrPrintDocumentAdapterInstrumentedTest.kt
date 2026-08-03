package com.local.matholickiosk.kiosk

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PrintAttributes
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.local.matholickiosk.kiosk.print.QrPrintCardRenderer
import com.local.matholickiosk.kiosk.print.QrPrintPdfWriter
import com.local.matholickiosk.kiosk.print.QrPdfExporter
import com.local.matholickiosk.kiosk.print.QrPdfShareIntentFactory
import com.local.matholickiosk.kiosk.qr.QrImageRenderer
import com.local.matholickiosk.kiosk.qr.QrTokenCodec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class QrPrintDocumentAdapterInstrumentedTest {
    @Test
    fun copiedQrBitmapIsWipedWhenWorkFailsBeforePdfExport() {
        val copiedQrBitmap = Bitmap.createBitmap(16, 16, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.BLACK)
        }

        val failure = runCatching {
            QrPdfExporter.consumeSensitiveBitmap(copiedQrBitmap) {
                throw IllegalStateException("synthetic audit failure")
            }
        }.exceptionOrNull()

        assertTrue(failure is IllegalStateException)
        assertTrue(copiedQrBitmap.isRecycled)
    }

    @Test
    fun sharedPdfCleanupDeletesTheExportAfterItsGracePeriod() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val exportDirectory = File(context.cacheDir, "qr_exports").apply { mkdirs() }
        val export = File(exportDirectory, "synthetic-cleanup-${System.nanoTime()}.pdf")
        export.writeText("synthetic non-QR fixture")

        try {
            QrPdfExporter.scheduleSharedFileCleanup(
                context = context,
                file = export,
                delayMillis = 50L,
            )

            val deadline = System.currentTimeMillis() + 5_000L
            while (export.exists() && System.currentTimeMillis() < deadline) {
                Thread.sleep(25L)
            }
            assertFalse(export.exists())
        } finally {
            export.delete()
        }
    }

    @Test
    fun abandonedShareIsDeletedAfterItsMaximumLifetime() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val exportDirectory = File(context.cacheDir, "qr_exports").apply { mkdirs() }
        val export = File(exportDirectory, "synthetic-expiry-${System.nanoTime()}.pdf")
        export.writeText("synthetic non-QR fixture")

        try {
            QrPdfExporter.scheduleSharedFileExpiry(
                context = context,
                file = export,
                delayMillis = 50L,
            )

            val deadline = System.currentTimeMillis() + 5_000L
            while (export.exists() && System.currentTimeMillis() < deadline) {
                Thread.sleep(25L)
            }
            assertFalse(export.exists())
        } finally {
            export.delete()
        }
    }

    @Test
    fun sharedPdfCleanupRefusesFilesOutsideTheExportDirectory() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val outsideExportDirectory = File(
            context.cacheDir,
            "synthetic-outside-cleanup-${System.nanoTime()}.pdf",
        )
        outsideExportDirectory.writeText("synthetic non-QR fixture")

        try {
            val failure = runCatching {
                QrPdfExporter.scheduleSharedFileCleanup(
                    context = context,
                    file = outsideExportDirectory,
                    delayMillis = 0L,
                )
            }.exceptionOrNull()

            assertTrue(failure is IllegalArgumentException)
            assertTrue(outsideExportDirectory.exists())
        } finally {
            outsideExportDirectory.delete()
        }
    }

    @Test
    fun pdfShareGrantsReadAccessToTheStreamAndClipDataUri() {
        val uri = Uri.parse(
            "content://com.local.matholickiosk.kiosk.files/qr_exports/synthetic-card.pdf",
        )
        val share = QrPdfShareIntentFactory.create(uri, "가상학생 전체이름")

        assertEquals(Intent.ACTION_SEND, share.action)
        assertEquals("application/pdf", share.type)
        assertEquals(uri, share.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java))
        assertEquals("학생 QR 카드 · 가상학생 전체이름", share.getStringExtra(Intent.EXTRA_SUBJECT))
        assertTrue(share.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0)
        assertEquals(1, share.clipData?.itemCount)
        assertEquals(uri, share.clipData?.getItemAt(0)?.uri)

        val chooser = Intent.createChooser(share, "PDF 공유")
        assertTrue(chooser.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0)
        assertEquals(uri, chooser.clipData?.getItemAt(0)?.uri)
    }

    @Test
    fun quickShareTargetsSamsungShareLiveWithoutChangingPdfGrantContract() {
        val uri = Uri.parse(
            "content://com.local.matholickiosk.kiosk.files/qr_exports/synthetic-card.pdf",
        )
        val share = QrPdfShareIntentFactory.createQuickShare(uri, "가상학생 전체이름")

        assertEquals(Intent.ACTION_SEND, share.action)
        assertEquals("application/pdf", share.type)
        assertEquals(QrPdfShareIntentFactory.SAMSUNG_QUICK_SHARE_PACKAGE, share.`package`)
        assertEquals(uri, share.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java))
        assertTrue(share.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0)
        assertEquals(uri, share.clipData?.getItemAt(0)?.uri)
    }

    @Test
    fun physicalCardAndQrSizesUsePostScriptPointsIndependentOfPrinterDpi() {
        val card = QrPrintCardRenderer.cardSizePoints()
        val qr = QrPrintCardRenderer.qrSizePoints()

        assertEquals(55f, card.width / 72f * 25.4f, 0.01f)
        assertEquals(80f, card.height / 72f * 25.4f, 0.01f)
        assertEquals(40f, qr.width / 72f * 25.4f, 0.01f)
        assertEquals(40f, qr.height / 72f * 25.4f, 0.01f)
    }

    @Test
    fun enlargedQrKeepsFullNameBelowIt() {
        val layout = QrPrintCardRenderer.layout(Rect(0, 0, 1_000, 1_000))
        val pointsToMillimeters = 25.4f / 72f

        assertEquals(
            18f,
            (layout.qr.top - layout.card.top) * pointsToMillimeters,
            0.01f,
        )
        assertEquals(40f, layout.qr.width() * pointsToMillimeters, 0.01f)
        assertTrue(layout.nameBaseline > layout.qr.bottom)
        assertEquals(
            10f,
            (layout.nameBaseline - layout.qr.bottom) * pointsToMillimeters,
            0.01f,
        )
        assertEquals(
            68f,
            (layout.nameBaseline - layout.card.top) * pointsToMillimeters,
            0.01f,
        )
    }

    @Test
    fun printableAreaSmallerThanTheCardIsRejectedInsteadOfScaled() {
        val output = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val qr = Bitmap.createBitmap(16, 16, Bitmap.Config.ARGB_8888)
        try {
            val failure = runCatching {
                QrPrintCardRenderer.draw(
                    canvas = Canvas(output),
                    contentRect = Rect(0, 0, output.width, output.height),
                    displayName = "가상학생 전체이름",
                    qrBitmap = qr,
                )
            }.exceptionOrNull()

            assertTrue(failure is IllegalArgumentException)
            assertTrue(failure?.message?.contains("55×80mm") == true)
        } finally {
            output.recycle()
            qr.recycle()
        }
    }

    @Test
    fun writesOnePagePdfAndWipesOwnedBitmapOnFinish() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val output = File(context.cacheDir, "synthetic-qr-print-${System.nanoTime()}.pdf")
        val qrBitmap = QrImageRenderer.render(
            QrTokenCodec().issue().use { it.payload },
            720,
        ).copy(Bitmap.Config.ARGB_8888, true)
        val attributes = PrintAttributes.Builder()
            .setMediaSize(PrintAttributes.MediaSize.ISO_A4.asPortrait())
            .setResolution(PrintAttributes.Resolution("test", "test", 600, 300))
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
                        displayName = "가상학생 전체이름",
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
                            var minDarkX = preview.width
                            var minDarkY = preview.height
                            var maxDarkX = -1
                            var maxDarkY = -1
                            for (y in 0 until preview.height) {
                                for (x in 0 until preview.width) {
                                    val pixel = preview.getPixel(x, y)
                                    if (
                                        Color.red(pixel) < 128 &&
                                        Color.green(pixel) < 128 &&
                                        Color.blue(pixel) < 128
                                    ) {
                                        darkSamples += 1
                                        minDarkX = minOf(minDarkX, x)
                                        minDarkY = minOf(minDarkY, y)
                                        maxDarkX = maxOf(maxDarkX, x)
                                        maxDarkY = maxOf(maxDarkY, y)
                                    }
                                }
                            }
                            assertTrue(darkSamples > 100)
                            val renderedWidthMm =
                                (maxDarkX - minDarkX + 1) / 72f * 25.4f
                            val renderedHeightMm =
                                (maxDarkY - minDarkY + 1) / 72f * 25.4f
                            assertEquals(55f, renderedWidthMm, 1.5f)
                            assertEquals(80f, renderedHeightMm, 1.5f)
                        } finally {
                            preview.recycle()
                        }
                    }
                }
            }
        } finally {
            if (!qrBitmap.isRecycled) {
                qrBitmap.eraseColor(Color.WHITE)
                qrBitmap.recycle()
            }
            assertTrue(qrBitmap.isRecycled)
            output.delete()
        }
    }
}
