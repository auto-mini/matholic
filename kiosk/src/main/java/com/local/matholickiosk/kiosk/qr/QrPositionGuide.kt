package com.local.matholickiosk.kiosk.qr

data class QrFrameBounds(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,
)

enum class QrFrameGuidance {
    MOVE_LEFT,
    MOVE_RIGHT,
    MOVE_UP,
    MOVE_DOWN,
    MOVE_CLOSER,
    MOVE_FARTHER,
    CENTERED,
}

object QrPositionGuide {
    fun classify(
        bounds: QrFrameBounds,
        imageWidth: Int,
        imageHeight: Int,
        mirrorHorizontally: Boolean,
    ): QrFrameGuidance {
        if (
            imageWidth <= 0 || imageHeight <= 0 ||
            bounds.right <= bounds.left || bounds.bottom <= bounds.top
        ) {
            return QrFrameGuidance.CENTERED
        }

        var centerX = (bounds.left + bounds.right) / 2f
        if (mirrorHorizontally) centerX = imageWidth - centerX
        val centerY = (bounds.top + bounds.bottom) / 2f
        val normalizedX = centerX / imageWidth
        val normalizedY = centerY / imageHeight

        if (normalizedX < CENTER_MIN) return QrFrameGuidance.MOVE_RIGHT
        if (normalizedX > CENTER_MAX) return QrFrameGuidance.MOVE_LEFT
        if (normalizedY < CENTER_MIN) return QrFrameGuidance.MOVE_DOWN
        if (normalizedY > CENTER_MAX) return QrFrameGuidance.MOVE_UP

        val widthRatio = (bounds.right - bounds.left).toFloat() / imageWidth
        val heightRatio = (bounds.bottom - bounds.top).toFloat() / imageHeight
        val shortestRatio = minOf(widthRatio, heightRatio)
        val longestRatio = maxOf(widthRatio, heightRatio)
        if (shortestRatio < MIN_SIZE_RATIO) return QrFrameGuidance.MOVE_CLOSER
        if (longestRatio > MAX_SIZE_RATIO) return QrFrameGuidance.MOVE_FARTHER
        return QrFrameGuidance.CENTERED
    }

    private const val CENTER_MIN = 0.35f
    private const val CENTER_MAX = 0.65f
    private const val MIN_SIZE_RATIO = 0.18f
    private const val MAX_SIZE_RATIO = 0.72f
}
