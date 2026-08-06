package com.local.matholickiosk.kiosk.domain

internal enum class StudentLoginPcStage {
    QR_VERIFIED,
    LOGIN_IN_PROGRESS,
}

internal object StudentLoginPcNotificationPolicy {
    fun shouldNotify(stage: StudentLoginPcStage): Boolean =
        stage == StudentLoginPcStage.QR_VERIFIED
}
