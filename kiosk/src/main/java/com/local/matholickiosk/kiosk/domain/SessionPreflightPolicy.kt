package com.local.matholickiosk.kiosk.domain

data class SessionPreflightInput(
    val deviceOwner: Boolean,
    val kioskPackagePermitted: Boolean,
    val webAppProtected: Boolean,
    val lockTaskMode: DedicatedDeviceMode,
    val policyConfigurationFailed: Boolean,
    val cameraPermissionGranted: Boolean,
    val cameraHardwareAvailable: Boolean,
    val batteryPercent: Int?,
    val usableStorageBytes: Long,
)

data class SessionPreflightResult(
    val blockingReasons: List<String>,
    val warnings: List<String>,
    val manualStudentSelectionRequired: Boolean,
) {
    val canStart: Boolean
        get() = blockingReasons.isEmpty()
}

object SessionPreflightPolicy {
    fun evaluate(input: SessionPreflightInput): SessionPreflightResult {
        val blockers = buildList {
            if (
                input.policyConfigurationFailed ||
                !input.deviceOwner ||
                !input.kioskPackagePermitted ||
                !input.webAppProtected ||
                input.lockTaskMode != DedicatedDeviceMode.LOCKED
            ) {
                add("기기 보안 정책이 정상 적용되지 않았습니다.")
            }
        }
        val cameraUnavailable =
            !input.cameraPermissionGranted || !input.cameraHardwareAvailable
        val warnings = buildList {
            if (cameraUnavailable) {
                add("카메라를 사용할 수 없어 QR은 차단되고 관리자 수동 선택만 가능합니다.")
            }
            input.batteryPercent
                ?.takeIf { it < LOW_BATTERY_PERCENT }
                ?.let { add("배터리가 ${it}%입니다. 충전기를 연결하세요.") }
            if (input.usableStorageBytes < LOW_STORAGE_BYTES) {
                add("저장 공간이 부족합니다. QR PDF 저장·인쇄가 실패할 수 있습니다.")
            }
            add("프린터 연결 상태는 Android 인쇄 화면에서 확인하세요.")
        }
        return SessionPreflightResult(
            blockingReasons = blockers,
            warnings = warnings,
            manualStudentSelectionRequired = cameraUnavailable,
        )
    }

    private const val LOW_BATTERY_PERCENT = 20
    private const val LOW_STORAGE_BYTES = 512L * 1024L * 1024L
}
