package com.local.matholickiosk.kiosk.print

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.os.CancellationSignal
import android.print.PrintAttributes
import android.print.pdf.PrintedPdfDocument
import java.io.FileDescriptor
import java.io.FileOutputStream

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
        borderColor: Int = Color.BLACK,
        borderStrokeWidthPoints: Float = millimetersToPoints(DEFAULT_BORDER_STROKE_MM),
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
            color = borderColor
            style = Paint.Style.STROKE
            strokeWidth = borderStrokeWidthPoints
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

    internal fun millimetersToPoints(millimeters: Float): Float =
        millimeters * POSTSCRIPT_POINTS_PER_INCH / MILLIMETERS_PER_INCH

    private const val CARD_WIDTH_MM = 55f
    private const val CARD_HEIGHT_MM = 80f
    private const val QR_SIZE_MM = 40f
    private const val QR_TOP_MM = 18f
    private const val NAME_BASELINE_MM = 68f
    private const val NAME_SIDE_MARGIN_MM = 4f
    private const val DEFAULT_BORDER_STROKE_MM = 0.35f
    private const val MILLIMETERS_PER_INCH = 25.4f
    private const val POSTSCRIPT_POINTS_PER_INCH = 72f
}
