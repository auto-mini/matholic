package com.local.matholickiosk.kiosk

import com.local.matholickiosk.kiosk.domain.ScannerCameraResumeAction
import com.local.matholickiosk.kiosk.domain.ScannerCameraResumePolicy
import org.junit.Assert.assertEquals
import org.junit.Test

class ScannerCameraResumePolicyTest {
    @Test
    fun foregroundScannerRebindsAfterCameraWasStopped() {
        assertEquals(
            ScannerCameraResumeAction.REBIND_CAMERA,
            ScannerCameraResumePolicy.decide(
                manualStudentSelectionOnly = false,
                cameraBound = false,
                activityStarted = true,
            ),
        )
    }

    @Test
    fun boundCameraOnlyReenablesAnalyzer() {
        assertEquals(
            ScannerCameraResumeAction.ENABLE_ANALYZER,
            ScannerCameraResumePolicy.decide(
                manualStudentSelectionOnly = false,
                cameraBound = true,
                activityStarted = true,
            ),
        )
    }

    @Test
    fun cameraIsNotStartedInBackgroundOrManualOnlyMode() {
        assertEquals(
            ScannerCameraResumeAction.NONE,
            ScannerCameraResumePolicy.decide(
                manualStudentSelectionOnly = false,
                cameraBound = false,
                activityStarted = false,
            ),
        )
        assertEquals(
            ScannerCameraResumeAction.NONE,
            ScannerCameraResumePolicy.decide(
                manualStudentSelectionOnly = true,
                cameraBound = false,
                activityStarted = true,
            ),
        )
    }
}
