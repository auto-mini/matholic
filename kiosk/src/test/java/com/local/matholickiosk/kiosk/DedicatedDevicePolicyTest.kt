package com.local.matholickiosk.kiosk

import com.local.matholickiosk.kiosk.domain.DedicatedDeviceMode
import com.local.matholickiosk.kiosk.domain.DedicatedDevicePolicy
import com.local.matholickiosk.kiosk.domain.DedicatedDeviceStatus
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class DedicatedDevicePolicyTest {
    @Test
    fun allowlistContainsOnlyKioskAndTrustedWebEngine() {
        assertArrayEquals(
            arrayOf("com.example.kiosk", "com.example.webpoc"),
            DedicatedDevicePolicy.allowlistedPackages(
                kioskPackage = "com.example.kiosk",
                webPocPackage = "com.example.webpoc",
            ),
        )
    }

    @Test
    fun lockedDeviceAndAdministratorExitHaveDistinctStatus() {
        assertEquals(
            "전용기기 잠금 활성",
            DedicatedDevicePolicy.statusLabel(
                DedicatedDeviceStatus(
                    isDeviceOwner = true,
                    isKioskPackagePermitted = true,
                    mode = DedicatedDeviceMode.LOCKED,
                ),
                administratorUnlocked = false,
            ),
        )
        assertEquals(
            "전용기기 · 관리자 잠금 해제",
            DedicatedDevicePolicy.statusLabel(
                DedicatedDeviceStatus(
                    isDeviceOwner = true,
                    isKioskPackagePermitted = true,
                    mode = DedicatedDeviceMode.NONE,
                ),
                administratorUnlocked = true,
            ),
        )
    }

    @Test
    fun missingDeviceOwnerIsReportedWithoutPretendingToBeLocked() {
        assertEquals(
            "전용기기 잠금 미설정",
            DedicatedDevicePolicy.statusLabel(
                DedicatedDeviceStatus(
                    isDeviceOwner = false,
                    isKioskPackagePermitted = false,
                    mode = DedicatedDeviceMode.NONE,
                ),
                administratorUnlocked = false,
            ),
        )
    }
}
