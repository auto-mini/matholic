[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$adb = 'C:\Users\user\AppData\Local\Android\Sdk\platform-tools\adb.exe'
$buildRoot = if ($env:MATHOLIC_BUILD_ROOT) {
    $env:MATHOLIC_BUILD_ROOT
} else {
    Join-Path $env:LOCALAPPDATA 'CodexBuild\matholic-kiosk'
}
$apk = Join-Path $buildRoot 'probe\outputs\apk\debug\probe-debug.apk'

if (-not (Test-Path -LiteralPath $adb)) { throw "adb not found: $adb" }
if (-not (Test-Path -LiteralPath $apk)) { throw 'Probe APK not found. Run scripts\build.ps1 first.' }

$deviceLines = @(& $adb devices | Select-Object -Skip 1 | Where-Object { $_ -match '\S' })
if ($deviceLines.Count -ne 1 -or $deviceLines[0] -notmatch "\tdevice$") {
    throw "Expected exactly one authorized device. adb devices returned: $($deviceLines -join '; ')"
}

& $adb install -r $apk
if ($LASTEXITCODE -ne 0) { throw "adb install failed with exit code $LASTEXITCODE" }

& $adb shell am start -n 'com.local.matholickiosk.probe.debug/com.local.matholickiosk.probe.MainActivity'
if ($LASTEXITCODE -ne 0) { throw "Probe launch failed with exit code $LASTEXITCODE" }

Write-Host 'Probe installed and launched. Enable its accessibility service on the tablet; do not use ADB to bypass the Android confirmation.'
