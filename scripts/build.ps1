[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$javaRoot = 'C:\Users\user\AppData\Local\Android\jdks\jdk-17.0.19+10'
$sdkRoot = 'C:\Users\user\AppData\Local\Android\Sdk'
$buildRoot = if ($env:MATHOLIC_BUILD_ROOT) {
    $env:MATHOLIC_BUILD_ROOT
} else {
    Join-Path $env:LOCALAPPDATA 'CodexBuild\matholic-kiosk'
}
$asciiParent = Join-Path $env:LOCALAPPDATA 'CodexWorkspaces'
$asciiRoot = Join-Path $asciiParent 'matholic-kiosk'

if (-not (Test-Path -LiteralPath $javaRoot)) { throw "JDK not found: $javaRoot" }
if (-not (Test-Path -LiteralPath $sdkRoot)) { throw "Android SDK not found: $sdkRoot" }

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

$env:JAVA_HOME = $javaRoot
$env:ANDROID_HOME = $sdkRoot

Push-Location $asciiRoot
try {
    & .\gradlew.bat clean :probe:testDebugUnitTest :poc:testDebugUnitTest :webpoc:testDebugUnitTest :kiosk:testDebugUnitTest :probe:lintDebug :poc:lintDebug :webpoc:lintDebug :kiosk:lintDebug :probe:assembleDebug :poc:assembleDebug :webpoc:assembleDebug :kiosk:assembleDebug
    if ($LASTEXITCODE -ne 0) { throw "Gradle failed with exit code $LASTEXITCODE" }
} finally {
    Pop-Location
}

Write-Host "Probe APK: $buildRoot\probe\outputs\apk\debug\probe-debug.apk"
Write-Host "Locked POC APK: $buildRoot\poc\outputs\apk\debug\poc-debug.apk"
Write-Host "Web POC APK: $buildRoot\webpoc\outputs\apk\debug\webpoc-debug.apk"
Write-Host "Gate 4 Kiosk APK: $buildRoot\kiosk\outputs\apk\debug\kiosk-debug.apk"
