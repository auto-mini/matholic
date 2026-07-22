[CmdletBinding()]
param(
    [switch]$SkipBuild,
    [switch]$SkipDeviceTests
)

$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'webpoc-device-common.ps1')
$adb = Get-WebPocAdbPath
$device = Resolve-WebPocDevice -AdbPath $adb -ExpectedModel 'SM-P610'

Write-Host "Device A confirmed: $($device.Model), Android $($device.Android), SDK $($device.Sdk)"

if (-not $SkipBuild) {
    & (Join-Path $PSScriptRoot 'build.ps1')
}

& (Join-Path $PSScriptRoot 'collect-device-baseline.ps1') `
    -Serial $device.Serial `
    -ExpectedModel 'SM-P610'

if ($SkipDeviceTests) {
    & (Join-Path $PSScriptRoot 'install-webpoc.ps1') `
        -Serial $device.Serial `
        -ExpectedModel 'SM-P610'
} else {
    & (Join-Path $PSScriptRoot 'test-webpoc-device.ps1') `
        -Serial $device.Serial `
        -ExpectedModel 'SM-P610'
}

Write-Host 'Device A is ready. Enter the dedicated test account only on the tablet screen.'
