package com.local.matholickiosk.kiosk.domain

enum class ScannerCameraResumeAction {
    REBIND_CAMERA,
    ENABLE_ANALYZER,
    NONE,
}

object ScannerCameraResumePolicy {
    fun decide(
        manualStudentSelectionOnly: Boolean,
        cameraBound: Boolean,
        activityStarted: Boolean,
    ): ScannerCameraResumeAction = when {
        manualStudentSelectionOnly -> ScannerCameraResumeAction.NONE
        cameraBound -> ScannerCameraResumeAction.ENABLE_ANALYZER
        activityStarted -> ScannerCameraResumeAction.REBIND_CAMERA
        else -> ScannerCameraResumeAction.NONE
    }
}
