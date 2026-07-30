package com.local.matholickiosk.kiosk.print

import android.content.Context
import android.graphics.Color
import android.graphics.RectF
import android.print.PrintAttributes
import android.print.pdf.PrintedPdfDocument
import java.io.File
import java.io.FileOutputStream
import kotlin.math.ceil

object BatchQrPdfExporter {
    fun export(
        context: Context,
        cards: List<BatchQrCard>,
    ): File {
        require(cards.isNotEmpty()) { "QR cards are required" }
        val directory = File(context.cacheDir, "qr_exports").apply { mkdirs() }
        val output = File(directory, "student-qr-cards-${System.currentTimeMillis()}.pdf")
        val attributes = PrintAttributes.Builder()
            .setMediaSize(PrintAttributes.MediaSize.ISO_A4.asPortrait())
            .setResolution(PrintAttributes.Resolution("pdf", "pdf", 300, 300))
            .setMinMargins(PrintAttributes.Margins(500, 500, 500, 500))
            .setColorMode(PrintAttributes.COLOR_MODE_MONOCHROME)
            .build()
        val document = PrintedPdfDocument(context.applicationContext, attributes)
        try {
            val pageCount = ceil(cards.size / CARDS_PER_PAGE.toDouble()).toInt()
            for (pageNumber in 0 until pageCount) {
                val page = document.startPage(pageNumber)
                page.canvas.drawColor(Color.WHITE)
                val content = page.info.contentRect
                val size = QrPrintCardRenderer.cardSizePoints()
                val gridWidth = size.width * COLUMNS
                val gridHeight = size.height * ROWS
                require(content.width() >= gridWidth && content.height() >= gridHeight) {
                    "A4 인쇄 가능 영역에 QR 카드를 배치할 수 없습니다."
                }
                val startX = content.left + (content.width() - gridWidth) / 2f
                val startY = content.top + (content.height() - gridHeight) / 2f
                cards.drop(pageNumber * CARDS_PER_PAGE)
                    .take(CARDS_PER_PAGE)
                    .forEachIndexed { index, card ->
                        val column = index % COLUMNS
                        val row = index / COLUMNS
                        val left = startX + column * size.width
                        val top = startY + row * size.height
                        QrPrintCardRenderer.drawCard(
                            canvas = page.canvas,
                            card = RectF(left, top, left + size.width, top + size.height),
                            displayName = card.displayName,
                            qrBitmap = card.qrBitmap,
                        )
                    }
                document.finishPage(page)
            }
            FileOutputStream(output).use(document::writeTo)
            return output
        } catch (failure: Throwable) {
            output.delete()
            throw failure
        } finally {
            document.close()
            cards.forEach { card -> QrPdfExporter.releaseSensitiveBitmap(card.qrBitmap) }
        }
    }

    private const val COLUMNS = 3
    private const val ROWS = 3
    private const val CARDS_PER_PAGE = COLUMNS * ROWS
}
