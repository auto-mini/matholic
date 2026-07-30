package com.local.matholickiosk.kiosk.qr

enum class QrFrameQuality {
    TOO_DARK,
    GLARE,
    LOW_CONTRAST,
}

object QrFrameQualityClassifier {
    fun classify(samples: IntArray): QrFrameQuality? {
        if (samples.isEmpty()) return null
        val mean = samples.sum().toFloat() / samples.size
        val minimum = samples.min()
        val maximum = samples.max()
        val brightRatio = samples.count { it >= 245 }.toFloat() / samples.size
        return when {
            mean < 42f -> QrFrameQuality.TOO_DARK
            brightRatio >= 0.28f -> QrFrameQuality.GLARE
            maximum - minimum < 38 -> QrFrameQuality.LOW_CONTRAST
            else -> null
        }
    }
}
