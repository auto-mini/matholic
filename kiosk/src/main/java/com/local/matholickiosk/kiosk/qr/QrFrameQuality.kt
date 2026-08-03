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
            mean < 50f -> QrFrameQuality.TOO_DARK
            brightRatio >= 0.08f -> QrFrameQuality.GLARE
            mean in 75f..220f && maximum - minimum < 38 -> QrFrameQuality.LOW_CONTRAST
            else -> null
        }
    }
}

class QrFrameQualityStabilizer(
    private val requiredConsecutiveFrames: Int = 3,
) {
    init {
        require(requiredConsecutiveFrames > 0)
    }

    private var candidate: QrFrameQuality? = null
    private var consecutiveFrames = 0
    private var lastDelivered: QrFrameQuality? = null
    private var normalFrameObservedAfterDark = true

    fun accept(quality: QrFrameQuality?): QrFrameQuality? {
        if (quality == null) {
            candidate = null
            consecutiveFrames = 0
            if (lastDelivered == QrFrameQuality.TOO_DARK) {
                normalFrameObservedAfterDark = true
            }
            return null
        }
        if (
            lastDelivered == QrFrameQuality.TOO_DARK &&
            !normalFrameObservedAfterDark &&
            quality != QrFrameQuality.TOO_DARK
        ) {
            candidate = null
            consecutiveFrames = 0
            return null
        }
        if (quality != candidate) {
            candidate = quality
            consecutiveFrames = 1
        } else {
            consecutiveFrames += 1
        }
        return quality.takeIf { consecutiveFrames >= requiredConsecutiveFrames }?.also {
            lastDelivered = it
            normalFrameObservedAfterDark = it != QrFrameQuality.TOO_DARK
        }
    }

    fun reset() {
        candidate = null
        consecutiveFrames = 0
        lastDelivered = null
        normalFrameObservedAfterDark = true
    }
}
