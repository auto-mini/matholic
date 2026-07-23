package com.local.matholickiosk.kiosk.domain

enum class KioskState {
    ADMIN_IDLE,
    PREFLIGHT,
    SESSION_SANITIZE,
    QR_READY,
    QR_VALIDATING,
    PRELOGIN_CHECK,
    LOGIN_FILL,
    LOGIN_SUBMIT,
    LOGIN_VERIFY,
    STUDENT_ACTIVE,
    INPUT_BLOCKED,
    LOGOUT_NAVIGATE,
    LOGOUT_SUBMIT,
    LOGOUT_VERIFY,
    RECOVERY_REQUIRED,
    LOCKED,
    MAINTENANCE_REQUIRED,
}

object KioskStatePolicy {
    private val sensitive = setOf(
        KioskState.SESSION_SANITIZE,
        KioskState.QR_VALIDATING,
        KioskState.PRELOGIN_CHECK,
        KioskState.LOGIN_FILL,
        KioskState.LOGIN_SUBMIT,
        KioskState.LOGIN_VERIFY,
        KioskState.STUDENT_ACTIVE,
        KioskState.INPUT_BLOCKED,
        KioskState.LOGOUT_NAVIGATE,
        KioskState.LOGOUT_SUBMIT,
        KioskState.LOGOUT_VERIFY,
    )

    fun afterProcessRestart(previous: KioskState): KioskState = when (previous) {
        KioskState.ADMIN_IDLE -> KioskState.ADMIN_IDLE
        KioskState.QR_READY -> KioskState.RECOVERY_REQUIRED
        KioskState.LOCKED -> KioskState.LOCKED
        KioskState.MAINTENANCE_REQUIRED -> KioskState.MAINTENANCE_REQUIRED
        in sensitive -> KioskState.LOCKED
        else -> KioskState.RECOVERY_REQUIRED
    }
}
