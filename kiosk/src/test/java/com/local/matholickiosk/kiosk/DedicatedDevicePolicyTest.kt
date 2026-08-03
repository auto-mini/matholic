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
            "보안 적용",
            DedicatedDevicePolicy.statusLabel(
                DedicatedDeviceStatus(
                    isDeviceOwner = true,
                    isKioskPackagePermitted = true,
                    isWebPocUninstallBlocked = true,
                    mode = DedicatedDeviceMode.LOCKED,
                ),
                administratorUnlocked = false,
            ),
        )
        assertEquals(
            "보안 일시 해제",
            DedicatedDevicePolicy.statusLabel(
                DedicatedDeviceStatus(
                    isDeviceOwner = true,
                    isKioskPackagePermitted = true,
                    isWebPocUninstallBlocked = true,
                    mode = DedicatedDeviceMode.NONE,
                ),
                administratorUnlocked = true,
            ),
        )
    }

    @Test
    fun missingDeviceOwnerIsReportedWithoutPretendingToBeLocked() {
        assertEquals(
            "보안 미설정",
            DedicatedDevicePolicy.statusLabel(
                DedicatedDeviceStatus(
                    isDeviceOwner = false,
                    isKioskPackagePermitted = false,
                    isWebPocUninstallBlocked = false,
                    mode = DedicatedDeviceMode.NONE,
                ),
                administratorUnlocked = false,
            ),
        )
    }

    @Test
    fun missingWebPocUninstallProtectionIsReportedAsPolicyError() {
        assertEquals(
            "보안 정책 오류",
            DedicatedDevicePolicy.statusLabel(
                DedicatedDeviceStatus(
                    isDeviceOwner = true,
                    isKioskPackagePermitted = true,
                    isWebPocUninstallBlocked = false,
                    mode = DedicatedDeviceMode.LOCKED,
                ),
                administratorUnlocked = false,
            ),
        )
    }
}
