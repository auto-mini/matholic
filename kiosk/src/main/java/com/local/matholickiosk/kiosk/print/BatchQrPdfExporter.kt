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
    internal data class CutSheetLayout(
        val startX: Float,
        val startY: Float,
        val cardWidth: Float,
        val cardHeight: Float,
        val gap: Float,
        val gridWidth: Float,
        val gridHeight: Float,
    )

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
            .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
            .setColorMode(PrintAttributes.COLOR_MODE_MONOCHROME)
            .build()
        val document = PrintedPdfDocument(context.applicationContext, attributes)
        try {
            val pageCount = ceil(cards.size / CARDS_PER_PAGE.toDouble()).toInt()
            for (pageNumber in 0 until pageCount) {
                val page = document.startPage(pageNumber)
                page.canvas.drawColor(Color.WHITE)
                val content = page.info.contentRect
                val layout = cutSheetLayout(
                    pageWidth = content.width().toFloat(),
                    pageHeight = content.height().toFloat(),
                )
                cards.drop(pageNumber * CARDS_PER_PAGE)
                    .take(CARDS_PER_PAGE)
                    .forEachIndexed { index, card ->
                        val column = index % COLUMNS
                        val row = index / COLUMNS
                        val left = content.left +
                            layout.startX + column * (layout.cardWidth + layout.gap)
                        val top = content.top +
                            layout.startY + row * (layout.cardHeight + layout.gap)
                        QrPrintCardRenderer.drawCard(
                            canvas = page.canvas,
                            card = RectF(
                                left,
                                top,
                                left + layout.cardWidth,
                                top + layout.cardHeight,
                            ),
                            displayName = card.displayName,
                            qrBitmap = card.qrBitmap,
                            borderColor = CUT_LINE_COLOR,
                            borderStrokeWidthPoints = CUT_LINE_WIDTH_POINTS,
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

    internal fun cutSheetLayout(pageWidth: Float, pageHeight: Float): CutSheetLayout {
        val card = QrPrintCardRenderer.cardSizePoints()
        val gap = QrPrintCardRenderer.millimetersToPoints(CARD_GAP_MM)
        val gridWidth = card.width * COLUMNS + gap * (COLUMNS - 1)
        val gridHeight = card.height * ROWS + gap * (ROWS - 1)
        require(pageWidth >= gridWidth && pageHeight >= gridHeight) {
            "A4 인쇄 가능 영역에 절단 여백을 포함한 QR 카드를 배치할 수 없습니다."
        }
        return CutSheetLayout(
            startX = (pageWidth - gridWidth) / 2f,
            startY = (pageHeight - gridHeight) / 2f,
            cardWidth = card.width,
            cardHeight = card.height,
            gap = gap,
            gridWidth = gridWidth,
            gridHeight = gridHeight,
        )
    }

    private const val COLUMNS = 3
    private const val ROWS = 3
    private const val CARDS_PER_PAGE = COLUMNS * ROWS
    private const val CARD_GAP_MM = 5f
    private const val CUT_LINE_WIDTH_POINTS = 0.25f
    private val CUT_LINE_COLOR = Color.rgb(173, 173, 173)
}
