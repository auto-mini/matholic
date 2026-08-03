package com.local.matholickiosk.kiosk.domain

enum class DedicatedDeviceMode {
    NONE,
    PINNED,
    LOCKED,
}

data class DedicatedDeviceStatus(
    val isDeviceOwner: Boolean,
    val isKioskPackagePermitted: Boolean,
    val isWebPocUninstallBlocked: Boolean,
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
            !status.isDeviceOwner -> "보안 미설정"
            !status.isKioskPackagePermitted || !status.isWebPocUninstallBlocked ->
                "보안 정책 오류"
            administratorUnlocked && status.mode == DedicatedDeviceMode.NONE ->
                "보안 일시 해제"
            status.mode == DedicatedDeviceMode.LOCKED -> "보안 적용"
            else -> "보안 준비 중"
        }
}
