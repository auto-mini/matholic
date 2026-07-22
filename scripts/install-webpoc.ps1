[CmdletBinding()]
param(
    [string]$Serial,
    [string]$ExpectedModel
)

$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'webpoc-device-common.ps1')
$adb = Get-WebPocAdbPath
$buildRoot = if ($env:MATHOLIC_BUILD_ROOT) {
    $env:MATHOLIC_BUILD_ROOT
} else {
    Join-Path $env:LOCALAPPDATA 'CodexBuild\matholic-kiosk'
}
$apk = Join-Path $buildRoot 'webpoc\outputs\apk\debug\webpoc-debug.apk'

if (-not (Test-Path -LiteralPath $apk)) { throw 'Web POC APK not found. Run scripts\build.ps1 first.' }

$device = Resolve-WebPocDevice -AdbPath $adb -Serial $Serial -ExpectedModel $ExpectedModel
Write-Host "Target confirmed: $($device.Model), Android $($device.Android), SDK $($device.Sdk)"

& $adb -s $device.Serial install -r $apk
if ($LASTEXITCODE -ne 0) { throw "adb install failed with exit code $LASTEXITCODE" }

& $adb -s $device.Serial shell am start -n 'com.local.matholickiosk.webpoc/com.local.matholickiosk.webpoc.MainActivity'
if ($LASTEXITCODE -ne 0) { throw "Web POC launch failed with exit code $LASTEXITCODE" }
Wait-WebPocIdle -AdbPath $adb -Serial $device.Serial

Write-Host 'Web POC installed and launched. Enter only the dedicated test account on the tablet.'
