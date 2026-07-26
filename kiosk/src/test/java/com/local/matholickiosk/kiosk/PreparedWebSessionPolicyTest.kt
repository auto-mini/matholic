package com.local.matholickiosk.kiosk

import org.junit.Assert.assertEquals
import org.junit.Test

class PreparedWebSessionPolicyTest {
    @Test
    fun visibleScannerLaunchesPreparedSession() {
        assertEquals(
            PreparedWebSessionDisposition.LAUNCH,
            PreparedWebSessionPolicy.decide(
                destroyed = false,
                scannerVisible = true,
            ),
        )
    }

    @Test
    fun leavingScannerCancelsPreparedSessionAndRestoresReadyState() {
        assertEquals(
            PreparedWebSessionDisposition.CANCEL_AND_RESTORE,
            PreparedWebSessionPolicy.decide(
                destroyed = false,
                scannerVisible = false,
            ),
        )
    }

    @Test
    fun destroyedActivityOnlyRevokesPreparedCredentials() {
        assertEquals(
            PreparedWebSessionDisposition.REVOKE_ONLY,
            PreparedWebSessionPolicy.decide(
                destroyed = true,
                scannerVisible = false,
            ),
        )
    }
}
