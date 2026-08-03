package com.local.matholickiosk.kiosk

import com.local.matholickiosk.kiosk.domain.SessionPreflightInput
import com.local.matholickiosk.kiosk.domain.SessionPreflightPolicy
import com.local.matholickiosk.kiosk.domain.DedicatedDeviceMode
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionPreflightPolicyTest {
    @Test
    fun `device policy failure blocks session start`() {
        val result = SessionPreflightPolicy.evaluate(healthyInput().copy(deviceOwner = false))

        assertFalse(result.canStart)
        assertTrue(result.blockingReasons.single().contains("보안 정책"))
    }

    @Test
    fun `lock task must be exactly locked`() {
        assertFalse(
            SessionPreflightPolicy.evaluate(
                healthyInput().copy(lockTaskMode = DedicatedDeviceMode.PINNED),
            ).canStart,
        )
        assertFalse(
            SessionPreflightPolicy.evaluate(
                healthyInput().copy(lockTaskMode = DedicatedDeviceMode.NONE),
            ).canStart,
        )
    }

    @Test
    fun `camera failure allows manual selection with warning`() {
        val result = SessionPreflightPolicy.evaluate(
            healthyInput().copy(cameraPermissionGranted = false),
        )

        assertTrue(result.canStart)
        assertTrue(result.manualStudentSelectionRequired)
        assertTrue(result.warnings.any { it.contains("수동 선택") })
    }

    @Test
    fun `low battery and storage only warn`() {
        val result = SessionPreflightPolicy.evaluate(
            healthyInput().copy(
                batteryPercent = 10,
                usableStorageBytes = 100L,
            ),
        )

        assertTrue(result.canStart)
        assertFalse(result.manualStudentSelectionRequired)
        assertTrue(result.warnings.any { it.contains("배터리") })
        assertTrue(result.warnings.any { it.contains("저장 공간") })
    }

    private fun healthyInput() = SessionPreflightInput(
        deviceOwner = true,
        kioskPackagePermitted = true,
        webAppProtected = true,
        lockTaskMode = DedicatedDeviceMode.LOCKED,
        policyConfigurationFailed = false,
        cameraPermissionGranted = true,
        cameraHardwareAvailable = true,
        batteryPercent = 80,
        usableStorageBytes = 2L * 1024L * 1024L * 1024L,
    )
}
