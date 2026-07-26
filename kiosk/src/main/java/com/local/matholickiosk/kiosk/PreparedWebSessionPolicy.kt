package com.local.matholickiosk.kiosk

internal enum class PreparedWebSessionDisposition {
    LAUNCH,
    CANCEL_AND_RESTORE,
    REVOKE_ONLY,
}

internal object PreparedWebSessionPolicy {
    fun decide(
        destroyed: Boolean,
        scannerVisible: Boolean,
    ): PreparedWebSessionDisposition = when {
        destroyed -> PreparedWebSessionDisposition.REVOKE_ONLY
        !scannerVisible -> PreparedWebSessionDisposition.CANCEL_AND_RESTORE
        else -> PreparedWebSessionDisposition.LAUNCH
    }
}
