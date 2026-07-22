package com.local.matholickiosk.webpoc

object WebFailurePolicy {
    fun shouldIgnorePageFinishedWhilePreflightRetryPending(
        state: WebPocState,
        retryScheduled: Boolean,
        retryStarted: Boolean,
    ): Boolean = state == WebPocState.PREFLIGHT && retryScheduled && !retryStarted

    fun canRetryPreflightLoginDns(
        isForMainFrame: Boolean,
        state: WebPocState,
        failureReason: String,
        retryStarted: Boolean,
    ): Boolean = isForMainFrame &&
        state == WebPocState.PREFLIGHT &&
        failureReason == "NETWORK_DNS_LOGIN" &&
        !retryStarted

    fun shouldLockForMainFrameFailure(
        isForMainFrame: Boolean,
        state: WebPocState,
    ): Boolean = isForMainFrame && state !in setOf(
        WebPocState.IDLE,
        WebPocState.LOCKED,
        WebPocState.MAINTENANCE_REQUIRED,
    )
}
