package com.local.matholickiosk.webpoc

object AdminRecoveryPolicy {
    fun isTrustedCaller(
        callerPackage: String?,
        expectedPackage: String,
        signaturesMatch: Boolean,
    ): Boolean =
        callerPackage == expectedPackage && signaturesMatch

    fun requiresSanitization(savedState: WebPocState): Boolean =
        savedState != WebPocState.IDLE
}
