package com.local.matholickiosk.webpoc

internal object RemoteSupportPolicy {
    const val DEFAULT_DURATION_MILLIS = 30L * 60L * 1_000L
    const val MIN_DURATION_MILLIS = 60L * 1_000L
    const val MAX_DURATION_MILLIS = 2L * 60L * 60L * 1_000L

    fun expiresAt(
        nowEpochMillis: Long,
        requestedDurationMillis: Long,
    ): Long {
        val duration = requestedDurationMillis.coerceIn(
            MIN_DURATION_MILLIS,
            MAX_DURATION_MILLIS,
        )
        return if (Long.MAX_VALUE - nowEpochMillis < duration) {
            Long.MAX_VALUE
        } else {
            nowEpochMillis + duration
        }
    }

    fun isActive(
        nowEpochMillis: Long,
        currentBootCount: Int,
        storedBootCount: Int,
        expiresAtEpochMillis: Long,
    ): Boolean =
        currentBootCount >= 0 &&
            storedBootCount == currentBootCount &&
            expiresAtEpochMillis > nowEpochMillis

    fun canCapture(supportActive: Boolean, sensitiveScreen: Boolean): Boolean =
        supportActive && !sensitiveScreen
}
