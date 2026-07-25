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
import android.print.PrintAttributes.Resolution
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.pdf.PrintedPdfDocument
import java.io.FileDescriptor
import java.io.FileOutputStream
import kotlin.math.min

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
        private const val FILE_NAME = "matholic-qr-card.pdf"
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
                resolution = attributes.resolution
                    ?: PrintAttributes.Resolution("fallback", "fallback", 300, 300),
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
    internal data class PixelDimensions(
        val width: Float,
        val height: Float,
    )

    fun draw(
        canvas: Canvas,
        contentRect: Rect,
        resolution: Resolution,
        displayName: String,
        qrBitmap: Bitmap,
    ) {
        canvas.drawColor(Color.WHITE)
        val availableWidth = contentRect.width().toFloat()
        val availableHeight = contentRect.height().toFloat()
        val targetCard = cardSizePixels(resolution)
        val fitScale = min(
            1f,
            min(
                availableWidth / targetCard.width,
                availableHeight / targetCard.height,
            ),
        )
        val cardWidth = targetCard.width * fitScale
        val cardHeight = targetCard.height * fitScale
        val left = contentRect.left + (availableWidth - cardWidth) / 2f
        val top = contentRect.top + (availableHeight - cardHeight) / 2f
        val card = RectF(left, top, left + cardWidth, top + cardHeight)
        val verticalPadding =
            millimetersToPixels(CARD_PADDING_MM, resolution.verticalDpi) * fitScale

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = millimetersToPixels(0.35f, resolution.horizontalDpi)
                .coerceAtLeast(1f)
        }
        canvas.drawRect(card, borderPaint)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textAlign = Paint.Align.CENTER
        }
        val centerX = card.centerX()
        textPaint.textSize = millimetersToPixels(5.2f, resolution.verticalDpi) * fitScale
        textPaint.isFakeBoldText = true
        canvas.drawText(
            displayName.trim().take(80),
            centerX,
            top + verticalPadding + textPaint.textSize,
            textPaint,
        )

        val headerBottom = top + verticalPadding + textPaint.textSize * 1.8f
        val qrSize = qrSizePixels(resolution, fitScale)
        val qrLeft = centerX - qrSize.width / 2f
        val qrTop = headerBottom + millimetersToPixels(4f, resolution.verticalDpi) * fitScale
        canvas.drawBitmap(
            qrBitmap,
            null,
            RectF(qrLeft, qrTop, qrLeft + qrSize.width, qrTop + qrSize.height),
            Paint().apply { isFilterBitmap = false },
        )

        textPaint.isFakeBoldText = false
        textPaint.textSize = millimetersToPixels(3.2f, resolution.verticalDpi) * fitScale
        canvas.drawText(
            "매쓰홀릭 채점 QR",
            centerX,
            qrTop + qrSize.height + millimetersToPixels(7f, resolution.verticalDpi) * fitScale,
            textPaint,
        )
    }

    internal fun cardSizePixels(
        resolution: Resolution,
        fitScale: Float = 1f,
    ): PixelDimensions = PixelDimensions(
        width = millimetersToPixels(CARD_WIDTH_MM, resolution.horizontalDpi) * fitScale,
        height = millimetersToPixels(CARD_HEIGHT_MM, resolution.verticalDpi) * fitScale,
    )

    internal fun qrSizePixels(
        resolution: Resolution,
        fitScale: Float = 1f,
    ): PixelDimensions = PixelDimensions(
        width = millimetersToPixels(QR_SIZE_MM, resolution.horizontalDpi) * fitScale,
        height = millimetersToPixels(QR_SIZE_MM, resolution.verticalDpi) * fitScale,
    )

    private fun millimetersToPixels(millimeters: Float, dpi: Int): Float =
        millimeters * dpi.toFloat() / MILLIMETERS_PER_INCH

    private const val CARD_WIDTH_MM = 65f
    private const val CARD_HEIGHT_MM = 90f
    private const val CARD_PADDING_MM = 5f
    private const val QR_SIZE_MM = 30f
    private const val MILLIMETERS_PER_INCH = 25.4f
}
