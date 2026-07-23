package com.local.matholickiosk.kiosk

import com.local.matholickiosk.kiosk.domain.KioskState
import com.local.matholickiosk.kiosk.domain.KioskStatePolicy
import org.junit.Assert.assertEquals
import org.junit.Test

class KioskStatePolicyTest {
    @Test
    fun restartDuringSensitiveAutomationFailsClosed() {
        listOf(
            KioskState.LOGIN_FILL,
            KioskState.LOGIN_SUBMIT,
            KioskState.LOGIN_VERIFY,
            KioskState.STUDENT_ACTIVE,
            KioskState.LOGOUT_SUBMIT,
        ).forEach {
            assertEquals(KioskState.LOCKED, KioskStatePolicy.afterProcessRestart(it))
        }
    }

    @Test
    fun restartFromQrReadyRequiresRecovery() {
        assertEquals(
            KioskState.RECOVERY_REQUIRED,
            KioskStatePolicy.afterProcessRestart(KioskState.QR_READY),
        )
    }

    @Test
    fun terminalStatesRemainTerminal() {
        assertEquals(
            KioskState.LOCKED,
            KioskStatePolicy.afterProcessRestart(KioskState.LOCKED),
        )
        assertEquals(
            KioskState.MAINTENANCE_REQUIRED,
            KioskStatePolicy.afterProcessRestart(KioskState.MAINTENANCE_REQUIRED),
        )
    }
}
