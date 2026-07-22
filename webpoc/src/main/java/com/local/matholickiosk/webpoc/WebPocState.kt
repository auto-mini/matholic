package com.local.matholickiosk.webpoc

enum class WebPocState {
    IDLE,
    PREFLIGHT,
    SESSION_SANITIZE,
    LOGIN_FILL,
    LOGIN_SUBMIT,
    LOGIN_VERIFY,
    ACTIVE,
    INPUT_BLOCKED,
    LOGOUT_NAVIGATE,
    LOGOUT_SUBMIT,
    LOGOUT_VERIFY,
    LOCKED,
    MAINTENANCE_REQUIRED,
    RECOVERY_REQUIRED,
    ;

    fun requiresRecoveryAfterRestart(): Boolean = this !in setOf(
        IDLE,
        PREFLIGHT,
        SESSION_SANITIZE,
        LOCKED,
        MAINTENANCE_REQUIRED,
    )
}
