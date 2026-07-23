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
import kotlin.math.min

class QrPrintDocumentAdapter(
    context: Context,
    private val maskedDisplayName: String,
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
                maskedDisplayName = maskedDisplayName,
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
        private const val FILE_NAME = "matholic-qr-card.pdf"
    }
}

internal object QrPrintPdfWriter {
    fun write(
        context: Context,
        attributes: PrintAttributes,
        maskedDisplayName: String,
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
                maskedDisplayName = maskedDisplayName,
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
    fun draw(
        canvas: Canvas,
        contentRect: Rect,
        maskedDisplayName: String,
        qrBitmap: Bitmap,
    ) {
        canvas.drawColor(Color.WHITE)
        val availableWidth = contentRect.width().toFloat()
        val availableHeight = contentRect.height().toFloat()
        val cardWidth = min(availableWidth * 0.82f, availableHeight * 0.68f)
        val cardHeight = min(availableHeight * 0.78f, cardWidth * 1.30f)
        val left = contentRect.left + (availableWidth - cardWidth) / 2f
        val top = contentRect.top + (availableHeight - cardHeight) / 2f
        val card = RectF(left, top, left + cardWidth, top + cardHeight)
        val padding = cardWidth * 0.07f

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = (cardWidth * 0.004f).coerceAtLeast(2f)
        }
        canvas.drawRect(card, borderPaint)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textAlign = Paint.Align.CENTER
        }
        val centerX = card.centerX()
        textPaint.textSize = cardWidth * 0.055f
        textPaint.isFakeBoldText = true
        canvas.drawText("매쓰홀릭 채점 QR", centerX, top + padding + textPaint.textSize, textPaint)

        textPaint.textSize = cardWidth * 0.075f
        canvas.drawText(
            maskedDisplayName.trim().take(80),
            centerX,
            top + padding * 1.7f + textPaint.textSize * 2f,
            textPaint,
        )

        val headerBottom = top + padding * 2.2f + textPaint.textSize * 2f
        val footerHeight = cardHeight * 0.13f
        val qrSize = min(
            cardWidth - padding * 2f,
            card.bottom - padding - footerHeight - headerBottom,
        )
        val qrLeft = centerX - qrSize / 2f
        val qrTop = headerBottom + (card.bottom - footerHeight - headerBottom - qrSize) / 2f
        canvas.drawBitmap(
            qrBitmap,
            null,
            RectF(qrLeft, qrTop, qrLeft + qrSize, qrTop + qrSize),
            Paint().apply { isFilterBitmap = false },
        )

        textPaint.isFakeBoldText = false
        textPaint.textSize = cardWidth * 0.032f
        canvas.drawText(
            "카드를 한 장씩 카메라에 보여주세요",
            centerX,
            card.bottom - padding - textPaint.textSize * 1.35f,
            textPaint,
        )
        canvas.drawText(
            "분실 시 선생님에게 알려주세요",
            centerX,
            card.bottom - padding,
            textPaint,
        )
    }
}
