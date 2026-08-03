[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [string]$Serial
)

$ErrorActionPreference = 'Stop'

if ($Serial -notmatch '^emulator-\d+$') {
    throw 'This destructive debug integration test accepts emulator serials only.'
}

$projectRoot = Split-Path -Parent $PSScriptRoot
$sdkRoot = Join-Path $env:LOCALAPPDATA 'Android\Sdk'
$javaRoot = Join-Path $env:LOCALAPPDATA 'Android\jdks\jdk-17.0.19+10'
$adb = Join-Path $sdkRoot 'platform-tools\adb.exe'
$buildRoot = Join-Path $env:LOCALAPPDATA 'CodexBuild\matholic-kiosk'
$kioskApk = Join-Path $buildRoot 'kiosk\outputs\apk\debug\kiosk-debug.apk'
$webPocApk = Join-Path $buildRoot 'webpoc\outputs\apk\debug\webpoc-debug.apk'
$kioskPackage = 'com.local.matholickiosk.kiosk'
$webPocPackage = 'com.local.matholickiosk.webpoc'
$probeComponent = "$kioskPackage/.TrustedWebCallerProbeActivity"
$resultFile = 'files/trusted-web-caller-result.txt'
$secureAction = 'com.local.matholickiosk.action.START_SECURE_WEB_SESSION'
$recoveryAction = 'com.local.matholickiosk.action.RECOVER_WEB_SESSION'

foreach ($path in @($adb, $javaRoot)) {
    if (-not (Test-Path -LiteralPath $path)) {
        throw "Required path not found: $path"
    }
}

$deviceState = ((& $adb -s $Serial get-state 2>$null) -join '').Trim()
$isEmulator = ((& $adb -s $Serial shell getprop ro.kernel.qemu 2>$null) -join '').Trim()
if ($deviceState -ne 'device' -or $isEmulator -ne '1') {
    throw "Serial is not a ready Android emulator: $Serial"
}

$env:JAVA_HOME = $javaRoot
$env:ANDROID_HOME = $sdkRoot
$env:ANDROID_SDK_ROOT = $sdkRoot
$env:ANDROID_SERIAL = $Serial

Push-Location $projectRoot
try {
    & .\gradlew.bat :kiosk:assembleDebug :webpoc:assembleDebug
    if ($LASTEXITCODE -ne 0) {
        throw 'Debug APK assembly failed.'
    }
} finally {
    Pop-Location
}

foreach ($apk in @($kioskApk, $webPocApk)) {
    if (-not (Test-Path -LiteralPath $apk)) {
        throw "Debug APK not found: $apk"
    }
}

& $adb -s $Serial install -r $kioskApk | Out-Host
if ($LASTEXITCODE -ne 0) {
    throw 'Kiosk debug install failed.'
}
& $adb -s $Serial install -r $webPocApk | Out-Host
if ($LASTEXITCODE -ne 0) {
    throw 'Web POC debug install failed.'
}

# This script is emulator-only, so resetting these synthetic app stores is intentional.
foreach ($packageName in @($kioskPackage, $webPocPackage)) {
    & $adb -s $Serial shell pm clear $packageName | Out-Null
    if ($LASTEXITCODE -ne 0) {
        throw "Could not reset disposable test package: $packageName"
    }
}

function Invoke-Probe(
    [string]$Action,
    [int]$ExpectedResultCode,
    [string]$ExpectedReason
) {
    & $adb -s $Serial shell am start -W `
        -n $probeComponent `
        --es probe_action $Action | Out-Null
    if ($LASTEXITCODE -ne 0) {
        throw "Could not start trusted caller probe for action: $Action"
    }

    $deadline = [DateTime]::UtcNow.AddSeconds(30)
    $content = ''
    while ([DateTime]::UtcNow -lt $deadline) {
        $content = (
            & $adb -s $Serial shell run-as $kioskPackage cat $resultFile 2>$null
        ) -join "`n"
        if ($LASTEXITCODE -eq 0 -and $content.Trim()) {
            break
        }
        Start-Sleep -Milliseconds 250
    }
    if (-not $content.Trim()) {
        throw "Trusted caller probe timed out for action: $Action"
    }

    $lines = $content -split "\r?\n"
    $actualResultCode = [int]$lines[0].Trim()
    $actualReason = if ($lines.Count -gt 1) { $lines[1].Trim() } else { '' }
    if ($actualResultCode -ne $ExpectedResultCode -or $actualReason -ne $ExpectedReason) {
        throw "Unexpected probe result for ${Action}: code=$actualResultCode reason=$actualReason"
    }
}

Invoke-Probe `
    -Action $secureAction `
    -ExpectedResultCode 0 `
    -ExpectedReason 'CREDENTIAL_BRIDGE_EMPTY'
Invoke-Probe `
    -Action $recoveryAction `
    -ExpectedResultCode -1 `
    -ExpectedReason ''

Write-Host 'Trusted Kiosk-to-Web caller integration passed.'
