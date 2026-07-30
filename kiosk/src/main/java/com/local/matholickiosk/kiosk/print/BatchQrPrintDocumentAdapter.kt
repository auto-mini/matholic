package com.local.matholickiosk.kiosk.print

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.RectF
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.pdf.PrintedPdfDocument
import java.io.FileOutputStream
import kotlin.math.ceil

data class BatchQrCard(
    val displayName: String,
    val qrBitmap: Bitmap,
)

class BatchQrPrintDocumentAdapter(
    context: Context,
    private val cards: List<BatchQrCard>,
) : PrintDocumentAdapter() {
    private val appContext = context.applicationContext
    private var attributes: PrintAttributes? = null
    private val pageCount: Int = ceil(cards.size / CARDS_PER_PAGE.toDouble()).toInt()

    init {
        require(cards.isNotEmpty()) { "QR cards are required" }
    }

    override fun onLayout(
        oldAttributes: PrintAttributes?,
        newAttributes: PrintAttributes,
        cancellationSignal: CancellationSignal,
        callback: LayoutResultCallback,
        extras: Bundle?,
    ) {
        if (cancellationSignal.isCanceled) {
            callback.onLayoutCancelled()
            return
        }
        attributes = newAttributes
        callback.onLayoutFinished(
            PrintDocumentInfo.Builder("student-qr-cards.pdf")
                .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                .setPageCount(pageCount)
                .build(),
            oldAttributes != newAttributes,
        )
    }

    override fun onWrite(
        pages: Array<out PageRange>,
        destination: ParcelFileDescriptor,
        cancellationSignal: CancellationSignal,
        callback: WriteResultCallback,
    ) {
        val configured = attributes
        if (configured == null) {
            callback.onWriteFailed("인쇄 설정이 없습니다.")
            return
        }
        val document = PrintedPdfDocument(appContext, configured)
        val writtenPages = mutableListOf<PageRange>()
        try {
            for (pageNumber in 0 until pageCount) {
                if (cancellationSignal.isCanceled) {
                    callback.onWriteCancelled()
                    return
                }
                if (pages.none { pageNumber in it.start..it.end }) continue
                val page = document.startPage(pageNumber)
                drawPage(
                    canvas = page.canvas,
                    contentRect = page.info.contentRect,
                    pageCards = cards.drop(pageNumber * CARDS_PER_PAGE)
                        .take(CARDS_PER_PAGE),
                )
                document.finishPage(page)
                writtenPages += PageRange(pageNumber, pageNumber)
            }
            FileOutputStream(destination.fileDescriptor).use(document::writeTo)
            callback.onWriteFinished(writtenPages.toTypedArray())
        } catch (failure: Throwable) {
            callback.onWriteFailed(failure.message ?: "QR 일괄 인쇄 문서 생성 실패")
        } finally {
            document.close()
        }
    }

    override fun onFinish() {
        cards.forEach { card ->
            if (!card.qrBitmap.isRecycled) {
                if (card.qrBitmap.isMutable) card.qrBitmap.eraseColor(Color.WHITE)
                card.qrBitmap.recycle()
            }
        }
        attributes = null
    }

    private fun drawPage(
        canvas: Canvas,
        contentRect: Rect,
        pageCards: List<BatchQrCard>,
    ) {
        canvas.drawColor(Color.WHITE)
        val size = QrPrintCardRenderer.cardSizePoints()
        val gridWidth = size.width * COLUMNS
        val gridHeight = size.height * ROWS
        require(
            contentRect.width() >= gridWidth && contentRect.height() >= gridHeight,
        ) {
            "A4 인쇄 가능 영역에 55×80mm 카드 9장을 배치할 수 없습니다."
        }
        val startX = contentRect.left + (contentRect.width() - gridWidth) / 2f
        val startY = contentRect.top + (contentRect.height() - gridHeight) / 2f
        pageCards.forEachIndexed { index, card ->
            val column = index % COLUMNS
            val row = index / COLUMNS
            val left = startX + column * size.width
            val top = startY + row * size.height
            QrPrintCardRenderer.drawCard(
                canvas = canvas,
                card = RectF(left, top, left + size.width, top + size.height),
                displayName = card.displayName,
                qrBitmap = card.qrBitmap,
            )
        }
    }

    companion object {
        internal const val COLUMNS = 3
        internal const val ROWS = 3
        internal const val CARDS_PER_PAGE = COLUMNS * ROWS
    }
}
