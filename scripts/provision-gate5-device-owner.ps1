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
$kioskPackage = 'com.local.matholickiosk.kiosk'
$webPocPackage = 'com.local.matholickiosk.webpoc'
$adminComponent = "$kioskPackage/.admin.KioskDeviceAdminReceiver"

foreach ($apk in @($kioskApk, $webPocApk)) {
    if (-not (Test-Path -LiteralPath $apk)) {
        throw "APK not found: $apk. Run scripts\build.ps1 first."
    }
}

$device = Resolve-WebPocDevice -AdbPath $adb -Serial $Serial -ExpectedModel $ExpectedModel
Write-Host "Target confirmed: $($device.Model), Android $($device.Android), SDK $($device.Sdk)"

$ownersBefore = (& $adb -s $device.Serial shell dpm list-owners) -join "`n"
$alreadyOwner = $ownersBefore -match [regex]::Escape($kioskPackage)
if (-not $alreadyOwner -and $ownersBefore -notmatch 'no owners') {
    throw 'Another device/profile owner is already configured.'
}

if (-not $alreadyOwner) {
    $users = ((& $adb -s $device.Serial shell pm list users) -join "`n")
    if (([regex]::Matches($users, 'UserInfo\{')).Count -ne 1) {
        throw 'Gate 5 provisioning requires exactly one Android user.'
    }

    $accounts = ((& $adb -s $device.Serial shell dumpsys account) -join "`n")
    $accountCountMatch = [regex]::Match($accounts, 'Accounts:\s*(\d+)')
    if (-not $accountCountMatch.Success) {
        throw 'Could not determine Android account count without exposing account details.'
    }
    if ([int]$accountCountMatch.Groups[1].Value -ne 0) {
        throw 'Gate 5 provisioning requires a freshly reset device with no accounts.'
    }
}

foreach ($apk in @($webPocApk, $kioskApk)) {
    & $adb -s $device.Serial install -r $apk
    if ($LASTEXITCODE -ne 0) { throw "adb install failed: $apk" }
}

& $adb -s $device.Serial shell pm grant $kioskPackage android.permission.CAMERA
if ($LASTEXITCODE -ne 0) { throw 'Camera permission grant failed.' }

if (-not $alreadyOwner) {
    & $adb -s $device.Serial shell dpm set-device-owner $adminComponent
    if ($LASTEXITCODE -ne 0) {
        throw 'Device Owner provisioning failed. Confirm factory reset, one user, and zero accounts.'
    }
}

& $adb -s $device.Serial shell am start -W -n "$kioskPackage/.MainActivity"
if ($LASTEXITCODE -ne 0) { throw 'Gate 5 kiosk launch failed.' }
Start-Sleep -Seconds 2

$ownersAfter = (& $adb -s $device.Serial shell dpm list-owners) -join "`n"
if ($ownersAfter -notmatch [regex]::Escape($kioskPackage)) {
    throw 'Kiosk is not the Device Owner after provisioning.'
}
$activityState = (& $adb -s $device.Serial shell dumpsys activity activities) -join "`n"
if ($activityState -notmatch 'mLockTaskModeState=LOCKED') {
    throw 'Kiosk launched but Lock Task mode is not LOCKED.'
}

Write-Host 'Gate 5 Device Owner provisioned.'
Write-Host 'Lock Task mode: LOCKED'
Write-Host 'Set the administrator PIN only on the tablet.'
