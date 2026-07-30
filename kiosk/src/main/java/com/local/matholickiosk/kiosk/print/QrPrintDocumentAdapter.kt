package com.local.matholickiosk.kiosk.print

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
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
import java.io.FileDescriptor
import java.io.FileOutputStream

class QrPrintDocumentAdapter(
    context: Context,
    private val displayName: String,
    private val qrBitmap: Bitmap,
) : PrintDocumentAdapter() {
    private val appContext = context.applicationContext
    private var printAttributes: PrintAttributes? = null

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
        printAttributes = newAttributes
        callback.onLayoutFinished(
            PrintDocumentInfo.Builder(FILE_NAME)
                .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                .setPageCount(1)
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
        if (cancellationSignal.isCanceled) {
            callback.onWriteCancelled()
            return
        }
        if (pages.none { it.containsPage(0) }) {
            callback.onWriteFinished(emptyArray())
            return
        }
        val attributes = printAttributes
        if (attributes == null) {
            callback.onWriteFailed("인쇄 설정이 없습니다.")
            return
        }

        try {
            val written = QrPrintPdfWriter.write(
                context = appContext,
                attributes = attributes,
                displayName = displayName,
                qrBitmap = qrBitmap,
                destination = destination.fileDescriptor,
                cancellationSignal = cancellationSignal,
            )
            if (!written) {
                callback.onWriteCancelled()
                return
            }
            callback.onWriteFinished(arrayOf(PageRange(0, 0)))
        } catch (failure: Throwable) {
            callback.onWriteFailed(failure.message ?: "QR 인쇄 문서 생성 실패")
        }
    }

    override fun onFinish() {
        if (!qrBitmap.isRecycled) {
            if (qrBitmap.isMutable) qrBitmap.eraseColor(Color.WHITE)
            qrBitmap.recycle()
        }
        printAttributes = null
    }

    private fun PageRange.containsPage(page: Int): Boolean =
        page in start..end

    companion object {
        private const val FILE_NAME = "student-qr-card.pdf"
    }
}

internal object QrPrintPdfWriter {
    fun write(
        context: Context,
        attributes: PrintAttributes,
        displayName: String,
        qrBitmap: Bitmap,
        destination: FileDescriptor,
        cancellationSignal: CancellationSignal,
    ): Boolean {
        val document = PrintedPdfDocument(context.applicationContext, attributes)
        return try {
            if (cancellationSignal.isCanceled) return false
            val page = document.startPage(0)
            QrPrintCardRenderer.draw(
                canvas = page.canvas,
                contentRect = page.info.contentRect,
                displayName = displayName,
                qrBitmap = qrBitmap,
            )
            document.finishPage(page)
            if (cancellationSignal.isCanceled) return false
            FileOutputStream(destination).use(document::writeTo)
            true
        } finally {
            document.close()
        }
    }
}

internal object QrPrintCardRenderer {
    internal data class PointDimensions(
        val width: Float,
        val height: Float,
    )

    internal data class CardLayout(
        val card: RectF,
        val qr: RectF,
        val nameBaseline: Float,
        val maximumNameWidth: Float,
    )

    fun draw(
        canvas: Canvas,
        contentRect: Rect,
        displayName: String,
        qrBitmap: Bitmap,
    ) {
        canvas.drawColor(Color.WHITE)
        val availableWidth = contentRect.width().toFloat()
        val availableHeight = contentRect.height().toFloat()
        val targetCard = cardSizePoints()
        require(
            availableWidth >= targetCard.width &&
                availableHeight >= targetCard.height,
        ) {
            "인쇄 가능 영역이 55×80mm QR 카드보다 작습니다."
        }
        val layout = layout(contentRect)
        drawCard(
            canvas = canvas,
            card = layout.card,
            displayName = displayName,
            qrBitmap = qrBitmap,
        )
    }

    fun drawCard(
        canvas: Canvas,
        card: RectF,
        displayName: String,
        qrBitmap: Bitmap,
    ) {
        val qrSize = qrSizePoints()
        val qrLeft = card.centerX() - qrSize.width / 2f
        val qrTop = card.top + millimetersToPoints(QR_TOP_MM)
        val layout = CardLayout(
            card = card,
            qr = RectF(qrLeft, qrTop, qrLeft + qrSize.width, qrTop + qrSize.height),
            nameBaseline = card.top + millimetersToPoints(NAME_BASELINE_MM),
            maximumNameWidth = card.width() - millimetersToPoints(NAME_SIDE_MARGIN_MM * 2f),
        )

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = millimetersToPoints(0.35f).coerceAtLeast(1f)
        }
        canvas.drawRect(layout.card, borderPaint)

        canvas.drawBitmap(
            qrBitmap,
            null,
            layout.qr,
            Paint().apply { isFilterBitmap = false },
        )

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textAlign = Paint.Align.CENTER
            textSize = millimetersToPoints(5.2f)
            isFakeBoldText = true
        }
        val displayText = displayName.trim().take(80)
        val measuredNameWidth = textPaint.measureText(displayText)
        if (measuredNameWidth > layout.maximumNameWidth) {
            textPaint.textSize *= layout.maximumNameWidth / measuredNameWidth
        }
        canvas.drawText(
            displayText,
            layout.card.centerX(),
            layout.nameBaseline,
            textPaint,
        )
    }

    internal fun layout(contentRect: Rect): CardLayout {
        val targetCard = cardSizePoints()
        val availableWidth = contentRect.width().toFloat()
        val availableHeight = contentRect.height().toFloat()
        val left = contentRect.left + (availableWidth - targetCard.width) / 2f
        val top = contentRect.top + (availableHeight - targetCard.height) / 2f
        val card = RectF(left, top, left + targetCard.width, top + targetCard.height)
        val qrSize = qrSizePoints()
        val qrLeft = card.centerX() - qrSize.width / 2f
        val qrTop = top + millimetersToPoints(QR_TOP_MM)
        return CardLayout(
            card = card,
            qr = RectF(qrLeft, qrTop, qrLeft + qrSize.width, qrTop + qrSize.height),
            nameBaseline = top + millimetersToPoints(NAME_BASELINE_MM),
            maximumNameWidth = targetCard.width - millimetersToPoints(NAME_SIDE_MARGIN_MM * 2f),
        )
    }

    internal fun cardSizePoints(): PointDimensions = PointDimensions(
        width = millimetersToPoints(CARD_WIDTH_MM),
        height = millimetersToPoints(CARD_HEIGHT_MM),
    )

    internal fun qrSizePoints(): PointDimensions = PointDimensions(
        width = millimetersToPoints(QR_SIZE_MM),
        height = millimetersToPoints(QR_SIZE_MM),
    )

    private fun millimetersToPoints(millimeters: Float): Float =
        millimeters * POSTSCRIPT_POINTS_PER_INCH / MILLIMETERS_PER_INCH

    private const val CARD_WIDTH_MM = 55f
    private const val CARD_HEIGHT_MM = 80f
    private const val QR_SIZE_MM = 30f
    private const val QR_TOP_MM = 28f
    private const val NAME_BASELINE_MM = 68f
    private const val NAME_SIDE_MARGIN_MM = 4f
    private const val MILLIMETERS_PER_INCH = 25.4f
    private const val POSTSCRIPT_POINTS_PER_INCH = 72f
}
