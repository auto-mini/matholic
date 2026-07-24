[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [string]$Serial,
    [string]$ExpectedModel = 'SM-P610',
    [string]$KioskApk = (Join-Path $PSScriptRoot '..\artifacts\matholic-kiosk-0.5.0-rc02-release.apk'),
    [string]$WebPocApk = (Join-Path $PSScriptRoot '..\artifacts\matholic-webpoc-0.3.5-rc02-release.apk'),
    [string]$SigningRoot = (Join-Path $env:LOCALAPPDATA 'MatholicKiosk\release-signing')
)

$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'webpoc-device-common.ps1')
$adb = Get-WebPocAdbPath
$sdkRoot = 'C:\Users\user\AppData\Local\Android\Sdk'
$javaRoot = 'C:\Users\user\AppData\Local\Android\jdks\jdk-17.0.19+10'
$apksigner = Join-Path $sdkRoot 'build-tools\37.0.0\apksigner.bat'
$keystorePath = Join-Path $SigningRoot 'matholic-kiosk-release.p12'
$markerPath = Join-Path $SigningRoot 'portable-recovery-confirmed.json'
$kioskPackage = 'com.local.matholickiosk.kiosk'
$webPocPackage = 'com.local.matholickiosk.webpoc'
$adminComponent = "$kioskPackage/.admin.KioskDeviceAdminReceiver"

$env:JAVA_HOME = $javaRoot
foreach ($path in @($KioskApk, $WebPocApk, $keystorePath, $markerPath, $apksigner)) {
    if (-not (Test-Path -LiteralPath $path)) {
        throw "Required production provisioning input not found: $path"
    }
}

& (Join-Path $PSScriptRoot 'verify-release-apks.ps1') `
    -KioskApk $KioskApk `
    -WebPocApk $WebPocApk
if ($LASTEXITCODE -ne 0) {
    throw "Release APK verification failed with exit code $LASTEXITCODE"
}

$marker = Get-Content -LiteralPath $markerPath -Raw | ConvertFrom-Json
foreach ($property in @(
    'backupKind',
    'backupReference',
    'backupSha256',
    'keystoreSha256',
    'signerSha256'
)) {
    if (-not $marker.$property) {
        throw "Recovery marker is missing property: $property"
    }
}
$localKeystoreHash = (Get-FileHash -LiteralPath $keystorePath -Algorithm SHA256).Hash
if (
    $localKeystoreHash -ne $marker.keystoreSha256 -or
    $marker.backupSha256 -ne $marker.keystoreSha256
) {
    throw 'Release keystore or portable backup hash does not match the confirmed recovery marker.'
}
if ($marker.backupKind -eq 'filesystem') {
    if (-not (Test-Path -LiteralPath $marker.backupReference)) {
        throw "Portable release key backup is unavailable: $($marker.backupReference)"
    }
    $backupKeystoreHash = (
        Get-FileHash -LiteralPath $marker.backupReference -Algorithm SHA256
    ).Hash
    if ($backupKeystoreHash -ne $marker.backupSha256) {
        throw 'Filesystem release key backup changed after recovery confirmation.'
    }
} elseif ($marker.backupKind -ne 'android-adb') {
    throw "Unsupported portable backup kind: $($marker.backupKind)"
}

$signatureOutput = (& $apksigner verify --print-certs $KioskApk 2>&1) -join "`n"
$apkSignerSha256 = [regex]::Match(
    $signatureOutput,
    'certificate SHA-256 digest:\s*([0-9a-fA-F]{64})'
).Groups[1].Value
if (
    -not $apkSignerSha256 -or
    $apkSignerSha256 -ne $marker.signerSha256
) {
    throw 'Release APK signer does not match the confirmed recovery key.'
}

$device = Resolve-WebPocDevice -AdbPath $adb -Serial $Serial -ExpectedModel $ExpectedModel
Write-Host "Target confirmed: $($device.Model), Android $($device.Android), SDK $($device.Sdk)"

$owners = (& $adb -s $device.Serial shell dpm list-owners) -join "`n"
if ($owners -notmatch 'no owners') {
    throw 'Production provisioning requires a freshly reset device with no device/profile owner.'
}

$users = ((& $adb -s $device.Serial shell pm list users) -join "`n")
if (([regex]::Matches($users, 'UserInfo\{')).Count -ne 1) {
    throw 'Production provisioning requires exactly one Android user.'
}
$accounts = ((& $adb -s $device.Serial shell dumpsys account) -join "`n")
$accountCountMatch = [regex]::Match($accounts, 'Accounts:\s*(\d+)')
if (-not $accountCountMatch.Success -or [int]$accountCountMatch.Groups[1].Value -ne 0) {
    throw 'Production provisioning requires a freshly reset device with zero Android accounts.'
}

foreach ($apk in @($WebPocApk, $KioskApk)) {
    & $adb -s $device.Serial install $apk
    if ($LASTEXITCODE -ne 0) {
        throw "Release APK installation failed: $apk"
    }
}

& $adb -s $device.Serial shell pm grant $kioskPackage android.permission.CAMERA
if ($LASTEXITCODE -ne 0) {
    throw 'Camera permission grant failed.'
}
& $adb -s $device.Serial shell dpm set-device-owner $adminComponent
if ($LASTEXITCODE -ne 0) {
    throw 'Release Device Owner provisioning failed.'
}
& $adb -s $device.Serial shell am start -W -n "$kioskPackage/.MainActivity"
if ($LASTEXITCODE -ne 0) {
    throw 'Release kiosk launch failed.'
}
Start-Sleep -Seconds 2

$ownersAfter = (& $adb -s $device.Serial shell dpm list-owners) -join "`n"
$activityState = (& $adb -s $device.Serial shell dumpsys activity activities) -join "`n"
if ($ownersAfter -notmatch [regex]::Escape($kioskPackage)) {
    throw 'Release Kiosk is not the Device Owner after provisioning.'
}
if ($activityState -notmatch 'mLockTaskModeState=LOCKED') {
    throw 'Release Kiosk launched but Lock Task mode is not LOCKED.'
}

Write-Host 'Release Device Owner provisioned and Lock Task mode is LOCKED.'
Write-Warning 'Complete the manual release smoke test before disabling USB debugging.'
