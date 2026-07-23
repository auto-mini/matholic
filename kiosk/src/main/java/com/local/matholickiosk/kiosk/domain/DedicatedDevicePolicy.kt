package com.local.matholickiosk.kiosk.domain

enum class DedicatedDeviceMode {
    NONE,
    PINNED,
    LOCKED,
}

data class DedicatedDeviceStatus(
    val isDeviceOwner: Boolean,
    val isKioskPackagePermitted: Boolean,
    val mode: DedicatedDeviceMode,
)

object DedicatedDevicePolicy {
    fun allowlistedPackages(
        kioskPackage: String,
        webPocPackage: String,
    ): Array<String> {
        require(kioskPackage.isNotBlank()) { "Kiosk package is required" }
        require(webPocPackage.isNotBlank()) { "Web POC package is required" }
        return arrayOf(kioskPackage, webPocPackage).distinct().toTypedArray()
    }

    fun statusLabel(
        status: DedicatedDeviceStatus,
        administratorUnlocked: Boolean,
    ): String =
        when {
            !status.isDeviceOwner -> "전용기기 잠금 미설정"
            administratorUnlocked && status.mode == DedicatedDeviceMode.NONE ->
                "전용기기 · 관리자 잠금 해제"
            status.mode == DedicatedDeviceMode.LOCKED -> "전용기기 잠금 활성"
            !status.isKioskPackagePermitted -> "전용기기 정책 오류"
            else -> "전용기기 잠금 준비 중"
        }
}
