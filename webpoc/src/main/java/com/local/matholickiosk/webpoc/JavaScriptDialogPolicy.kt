package com.local.matholickiosk.webpoc

object JavaScriptDialogPolicy {
    const val MAX_MESSAGE_LENGTH = 500
    const val ACTION_ARM_DELAY_MS = 800L

    fun canReplaceBrowserDialog(
        state: WebPocState,
        sourceUrl: String?,
        message: String?,
    ): Boolean {
        return state == WebPocState.ACTIVE &&
            WebSecurityPolicy.isAllowedStudentUrl(sourceUrl) &&
            !message.isNullOrBlank() &&
            message.length <= MAX_MESSAGE_LENGTH
    }
}
