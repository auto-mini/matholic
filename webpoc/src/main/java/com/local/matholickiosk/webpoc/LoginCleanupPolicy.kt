package com.local.matholickiosk.webpoc

object LoginCleanupPolicy {
    fun shouldCanonicalizeBeforeFingerprint(
        state: WebPocState,
        url: String?,
    ): Boolean {
        val isCleanupState = when (state) {
            WebPocState.LOGOUT_NAVIGATE,
            WebPocState.LOGOUT_SUBMIT,
            WebPocState.LOGOUT_VERIFY,
            WebPocState.RECOVERY_REQUIRED,
            -> true
            else -> false
        }
        return isCleanupState &&
            WebSecurityPolicy.isLoginUrl(url) &&
            !WebSecurityPolicy.isCanonicalLoginUrl(url)
    }
}
