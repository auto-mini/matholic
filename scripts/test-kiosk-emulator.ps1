[CmdletBinding()]
param(
    [string]$Serial = 'emulator-5554',
    [string]$TestClass
)

$ErrorActionPreference = 'Stop'
$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$javaRoot = 'C:\Users\user\AppData\Local\Android\jdks\jdk-17.0.19+10'
$sdkRoot = 'C:\Users\user\AppData\Local\Android\Sdk'
$adbPath = Join-Path $sdkRoot 'platform-tools\adb.exe'
$asciiParent = Join-Path $env:LOCALAPPDATA 'CodexWorkspaces'
$requiresAsciiJunction = $projectRoot -match '[^\x00-\x7F]'
$asciiRoot = if ($requiresAsciiJunction) {
    Join-Path $asciiParent 'matholic-kiosk'
} else {
    $projectRoot
}

if (-not $Serial.StartsWith('emulator-', [StringComparison]::Ordinal)) {
    throw "Refusing Android tests on a non-emulator serial: $Serial"
}
foreach ($requiredPath in @($javaRoot, $sdkRoot, $adbPath)) {
    if (-not (Test-Path -LiteralPath $requiredPath)) {
        throw "Required Android test dependency is missing: $requiredPath"
    }
}

$deviceLine = & $adbPath devices -l |
    Where-Object { $_ -match "^$([regex]::Escape($Serial))\s+device\b" }
if (-not $deviceLine) {
    throw "Authorized emulator is not connected: $Serial"
}
$isEmulator = ((& $adbPath -s $Serial shell getprop ro.kernel.qemu) -join '').Trim()
if ($isEmulator -ne '1') {
    throw "Refusing Android tests because $Serial is not an Android emulator."
}

if ($requiresAsciiJunction) {
    New-Item -ItemType Directory -Force -Path $asciiParent | Out-Null
    if (Test-Path -LiteralPath $asciiRoot) {
        $existing = Get-Item -LiteralPath $asciiRoot -Force
        $resolvedTarget = (Resolve-Path -LiteralPath $existing.Target).Path
        if ($resolvedTarget -ne $projectRoot) {
            throw "ASCII build junction points elsewhere: $asciiRoot -> $resolvedTarget"
        }
    } else {
        New-Item -ItemType Junction -Path $asciiRoot -Target $projectRoot | Out-Null
    }
}

$env:JAVA_HOME = $javaRoot
$env:ANDROID_HOME = $sdkRoot
$env:ANDROID_SERIAL = $Serial
$gradleArguments = @(':kiosk:connectedDebugAndroidTest')
if ($TestClass) {
    $gradleArguments +=
        "-Pandroid.testInstrumentationRunnerArguments.class=$TestClass"
}

Push-Location $asciiRoot
try {
    & .\gradlew.bat @gradleArguments
    if ($LASTEXITCODE -ne 0) {
        throw "Kiosk emulator tests failed with exit code $LASTEXITCODE"
    }
} finally {
    Pop-Location
}
