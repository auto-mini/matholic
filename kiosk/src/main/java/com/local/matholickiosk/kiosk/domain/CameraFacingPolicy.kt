package com.local.matholickiosk.kiosk.domain

enum class CameraFacing {
    FRONT,
    BACK,
}

object CameraFacingPolicy {
    fun choose(
        preferred: CameraFacing,
        frontAvailable: Boolean,
        backAvailable: Boolean,
    ): CameraFacing? = when {
        preferred == CameraFacing.FRONT && frontAvailable -> CameraFacing.FRONT
        preferred == CameraFacing.BACK && backAvailable -> CameraFacing.BACK
        frontAvailable -> CameraFacing.FRONT
        backAvailable -> CameraFacing.BACK
        else -> null
    }

    fun opposite(current: CameraFacing): CameraFacing = when (current) {
        CameraFacing.FRONT -> CameraFacing.BACK
        CameraFacing.BACK -> CameraFacing.FRONT
    }
}
