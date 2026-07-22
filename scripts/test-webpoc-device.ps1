[CmdletBinding()]
param(
    [string]$Serial,
    [string]$ExpectedModel
)

$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'webpoc-device-common.ps1')
$javaRoot = 'C:\Users\user\AppData\Local\Android\jdks\jdk-17.0.19+10'
$sdkRoot = 'C:\Users\user\AppData\Local\Android\Sdk'
$adb = Get-WebPocAdbPath -SdkRoot $sdkRoot
$asciiRoot = Join-Path $env:LOCALAPPDATA 'CodexWorkspaces\matholic-kiosk'
$buildRoot = if ($env:MATHOLIC_BUILD_ROOT) {
    $env:MATHOLIC_BUILD_ROOT
} else {
    Join-Path $env:LOCALAPPDATA 'CodexBuild\matholic-kiosk'
}
$apk = Join-Path $buildRoot 'webpoc\outputs\apk\debug\webpoc-debug.apk'

if (-not (Test-Path -LiteralPath $javaRoot)) { throw "JDK not found: $javaRoot" }
if (-not (Test-Path -LiteralPath $sdkRoot)) { throw "Android SDK not found: $sdkRoot" }
if (-not (Test-Path -LiteralPath $asciiRoot)) { throw 'ASCII build junction not found. Run scripts\build.ps1 first.' }

$device = Resolve-WebPocDevice -AdbPath $adb -Serial $Serial -ExpectedModel $ExpectedModel
Write-Host "Target confirmed: $($device.Model), Android $($device.Android), SDK $($device.Sdk)"

$env:JAVA_HOME = $javaRoot
$env:ANDROID_HOME = $sdkRoot
$previousAndroidSerial = $env:ANDROID_SERIAL
$env:ANDROID_SERIAL = $device.Serial
$previousStayOn = (@(
    & $adb -s $device.Serial shell settings get global stay_on_while_plugged_in
) -join '').Trim()
& $adb -s $device.Serial shell svc power stayon usb
if ($LASTEXITCODE -ne 0) { throw 'Failed to enable temporary USB stay-awake mode.' }
& $adb -s $device.Serial shell input keyevent KEYCODE_WAKEUP
if ($LASTEXITCODE -ne 0) { throw 'Failed to wake the device for visibility tests.' }

Push-Location $asciiRoot
$testExitCode = 1
try {
    & .\gradlew.bat :webpoc:connectedDebugAndroidTest
    $testExitCode = $LASTEXITCODE
} finally {
    Pop-Location
    if ($null -eq $previousAndroidSerial) {
        Remove-Item Env:ANDROID_SERIAL -ErrorAction SilentlyContinue
    } else {
        $env:ANDROID_SERIAL = $previousAndroidSerial
    }
    if ($previousStayOn -match '^\d+$') {
        & $adb -s $device.Serial shell settings put global stay_on_while_plugged_in $previousStayOn
    } else {
        & $adb -s $device.Serial shell settings delete global stay_on_while_plugged_in
    }
}

if (-not (Test-Path -LiteralPath $apk)) { throw "Web POC APK not found after tests: $apk" }
& $adb -s $device.Serial install -r $apk
if ($LASTEXITCODE -ne 0) { throw "Web POC reinstall failed with exit code $LASTEXITCODE" }
& $adb -s $device.Serial shell am start -n 'com.local.matholickiosk.webpoc/com.local.matholickiosk.webpoc.MainActivity'
if ($LASTEXITCODE -ne 0) { throw "Web POC launch failed with exit code $LASTEXITCODE" }
Wait-WebPocIdle -AdbPath $adb -Serial $device.Serial

if ($testExitCode -ne 0) { throw "Device tests failed with exit code $testExitCode; Web POC was reinstalled." }

Write-Host 'Web POC device tests passed; app reinstalled and launched.'
