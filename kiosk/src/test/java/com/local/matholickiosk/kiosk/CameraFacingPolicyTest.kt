package com.local.matholickiosk.kiosk

import com.local.matholickiosk.kiosk.domain.CameraFacing
import com.local.matholickiosk.kiosk.domain.CameraFacingPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CameraFacingPolicyTest {
    @Test
    fun frontIsUsedWhenPreferredAndAvailable() {
        assertEquals(
            CameraFacing.FRONT,
            CameraFacingPolicy.choose(
                preferred = CameraFacing.FRONT,
                frontAvailable = true,
                backAvailable = true,
            ),
        )
    }

    @Test
    fun unavailablePreferenceFallsBackToAvailableCamera() {
        assertEquals(
            CameraFacing.BACK,
            CameraFacingPolicy.choose(
                preferred = CameraFacing.FRONT,
                frontAvailable = false,
                backAvailable = true,
            ),
        )
        assertEquals(
            CameraFacing.FRONT,
            CameraFacingPolicy.choose(
                preferred = CameraFacing.BACK,
                frontAvailable = true,
                backAvailable = false,
            ),
        )
    }

    @Test
    fun noCameraReturnsNull() {
        assertNull(
            CameraFacingPolicy.choose(
                preferred = CameraFacing.FRONT,
                frontAvailable = false,
                backAvailable = false,
            ),
        )
    }

    @Test
    fun oppositeTogglesFacing() {
        assertEquals(CameraFacing.BACK, CameraFacingPolicy.opposite(CameraFacing.FRONT))
        assertEquals(CameraFacing.FRONT, CameraFacingPolicy.opposite(CameraFacing.BACK))
    }
}
