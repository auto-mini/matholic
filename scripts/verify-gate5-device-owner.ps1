[CmdletBinding()]
param(
    [string]$Serial,
    [string]$ExpectedModel = 'SM-P610'
)

$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'webpoc-device-common.ps1')
$adb = Get-WebPocAdbPath
$device = Resolve-WebPocDevice -AdbPath $adb -Serial $Serial -ExpectedModel $ExpectedModel
$kioskPackage = 'com.local.matholickiosk.kiosk'
$webPocPackage = 'com.local.matholickiosk.webpoc'

$owners = (& $adb -s $device.Serial shell dpm list-owners) -join "`n"
if ($owners -notmatch [regex]::Escape($kioskPackage)) {
    throw 'Kiosk is not the Device Owner.'
}

$policy = (& $adb -s $device.Serial shell dumpsys device_policy) -join "`n"
foreach ($packageName in @($kioskPackage, $webPocPackage)) {
    if ($policy -notmatch [regex]::Escape($packageName)) {
        throw "Lock Task policy does not contain $packageName."
    }
}

$activity = (& $adb -s $device.Serial shell dumpsys activity activities) -join "`n"
$mode = [regex]::Match($activity, 'mLockTaskModeState=(\w+)').Groups[1].Value
if (-not $mode) { throw 'Could not read Lock Task mode.' }

$packageDump = (& $adb -s $device.Serial shell dumpsys package $kioskPackage) -join "`n"
$versionName = [regex]::Match($packageDump, 'versionName=([^\s]+)').Groups[1].Value
$versionCode = [regex]::Match($packageDump, 'versionCode=(\d+)').Groups[1].Value

Write-Host "Target: $($device.Model), Android $($device.Android), SDK $($device.Sdk)"
Write-Host "Device Owner: $kioskPackage"
Write-Host "Kiosk version: $versionName / code $versionCode"
Write-Host "Lock Task mode: $mode"
Write-Host "Allowlisted packages: $kioskPackage, $webPocPackage"
