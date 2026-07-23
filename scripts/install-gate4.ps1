[CmdletBinding()]
param(
    [string]$Serial,
    [string]$ExpectedModel = 'SM-P610'
)

$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'webpoc-device-common.ps1')
$adb = Get-WebPocAdbPath
$buildRoot = if ($env:MATHOLIC_BUILD_ROOT) {
    $env:MATHOLIC_BUILD_ROOT
} else {
    Join-Path $env:LOCALAPPDATA 'CodexBuild\matholic-kiosk'
}
$kioskApk = Join-Path $buildRoot 'kiosk\outputs\apk\debug\kiosk-debug.apk'
$webPocApk = Join-Path $buildRoot 'webpoc\outputs\apk\debug\webpoc-debug.apk'

foreach ($apk in @($kioskApk, $webPocApk)) {
    if (-not (Test-Path -LiteralPath $apk)) {
        throw "APK not found: $apk. Run scripts\build.ps1 first."
    }
}

$device = Resolve-WebPocDevice -AdbPath $adb -Serial $Serial -ExpectedModel $ExpectedModel
Write-Host "Target confirmed: $($device.Model), Android $($device.Android), SDK $($device.Sdk)"

foreach ($apk in @($kioskApk, $webPocApk)) {
    & $adb -s $device.Serial install -r $apk
    if ($LASTEXITCODE -ne 0) { throw "adb install failed: $apk" }
}

& $adb -s $device.Serial shell pm grant com.local.matholickiosk.kiosk android.permission.CAMERA
if ($LASTEXITCODE -ne 0) { throw 'Camera permission grant failed' }

$permissionRegistry = (& $adb -s $device.Serial shell dumpsys package permissions) -join "`n"
$webPocPackage = (& $adb -s $device.Serial shell dumpsys package com.local.matholickiosk.webpoc) -join "`n"
if (
    $permissionRegistry -notmatch 'Permission \[com\.local\.matholickiosk\.permission\.CREDENTIAL_BRIDGE\]' -or
    $webPocPackage -notmatch 'com\.local\.matholickiosk\.permission\.CREDENTIAL_BRIDGE: granted=true'
) {
    throw 'Signature credential bridge permission was not registered'
}

& $adb -s $device.Serial shell am start -W -n 'com.local.matholickiosk.kiosk/com.local.matholickiosk.kiosk.MainActivity'
if ($LASTEXITCODE -ne 0) { throw 'Gate 4 kiosk launch failed' }

Write-Host 'Gate 4 alpha installed. Set the administrator PIN only on the tablet.'
