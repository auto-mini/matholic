package com.local.matholickiosk.webpoc

internal object DiagnosticEventPolicy {
    private const val SLOW_STAGE_THRESHOLD_MS = 8_000L
    private val slowStageStates = setOf(
        WebPocState.PREFLIGHT,
        WebPocState.SESSION_SANITIZE,
        WebPocState.LOGIN_FILL,
        WebPocState.LOGIN_SUBMIT,
        WebPocState.LOGIN_VERIFY,
        WebPocState.INPUT_BLOCKED,
        WebPocState.LOGOUT_NAVIGATE,
        WebPocState.LOGOUT_SUBMIT,
        WebPocState.LOGOUT_VERIFY,
        WebPocState.RECOVERY_REQUIRED,
    )
    private val resultReasons = setOf(
        "INCOMPLETE_RESULT",
        "HYDRATING_RESULT",
        "NOT_RESULT",
    )

    fun interruptedSession(state: WebPocState): String? =
        state.takeIf(WebPocState::requiresRecoveryAfterRestart)
            ?.let { "INTERRUPTED_SESSION:${it.name}" }

    fun slowStage(state: WebPocState, elapsedMs: Long): String? {
        if (state !in slowStageStates || elapsedMs < SLOW_STAGE_THRESHOLD_MS) return null
        val elapsedSeconds = (elapsedMs / 1_000L).coerceIn(0L, 999L)
        return "SLOW_STAGE:${state.name}:${elapsedSeconds}S"
    }

    fun resultIncomplete(
        reason: String?,
        expectedProblems: Int,
        classifiedCount: Int,
        hydrationPolls: Int,
        extractionFailures: Int,
    ): String {
        val safeReason = reason?.takeIf { it in resultReasons } ?: "UNKNOWN"
        return "RESULT_INCOMPLETE:$safeReason:" +
            "E${expectedProblems.coerceIn(0, 999)}:" +
            "C${classifiedCount.coerceIn(0, 999)}:" +
            "H${hydrationPolls.coerceIn(0, 999)}:" +
            "F${extractionFailures.coerceIn(0, 999)}"
    }
}
