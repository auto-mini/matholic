[CmdletBinding()]
param(
    [string]$SigningRoot = (Join-Path $env:LOCALAPPDATA 'MatholicKiosk\release-signing')
)

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
$keystorePath = Join-Path $SigningRoot 'matholic-kiosk-release.p12'
$credentialPath = Join-Path $SigningRoot 'matholic-kiosk-release.credential.clixml'
$artifactRoot = Join-Path $projectRoot 'artifacts'
$kioskApk = Join-Path $buildRoot 'kiosk\outputs\apk\release\kiosk-release.apk'
$webPocApk = Join-Path $buildRoot 'webpoc\outputs\apk\release\webpoc-release.apk'
$kioskArtifact = Join-Path $artifactRoot 'matholic-kiosk-0.5.0-rc02-release.apk'
$webPocArtifact = Join-Path $artifactRoot 'matholic-webpoc-0.3.5-rc02-release.apk'
$checksumFile = Join-Path $artifactRoot 'RELEASE_SHA256SUMS.txt'

foreach ($path in @($javaRoot, $sdkRoot, $keystorePath, $credentialPath)) {
    if (-not (Test-Path -LiteralPath $path)) {
        throw "Required release build input not found: $path"
    }
}

$credential = Import-Clixml -LiteralPath $credentialPath
if ($credential -isnot [pscredential]) {
    throw "Invalid DPAPI signing credential: $credentialPath"
}
$password = $credential.GetNetworkCredential().Password

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
$env:MATHOLIC_RELEASE_STORE_FILE = $keystorePath
$env:MATHOLIC_RELEASE_STORE_PASSWORD = $password
$env:MATHOLIC_RELEASE_KEY_ALIAS = $credential.UserName
$env:MATHOLIC_RELEASE_KEY_PASSWORD = $password

try {
    Push-Location $asciiRoot
    try {
        & .\gradlew.bat `
            --no-daemon `
            --no-configuration-cache `
            clean `
            :webpoc:testDebugUnitTest `
            :kiosk:testDebugUnitTest `
            :webpoc:lintRelease `
            :kiosk:lintRelease `
            :webpoc:assembleRelease `
            :kiosk:assembleRelease
        if ($LASTEXITCODE -ne 0) {
            throw "Release Gradle build failed with exit code $LASTEXITCODE"
        }
    } finally {
        Pop-Location
    }

    & (Join-Path $PSScriptRoot 'verify-release-apks.ps1') `
        -KioskApk $kioskApk `
        -WebPocApk $webPocApk
    if ($LASTEXITCODE -ne 0) {
        throw "Release APK verification failed with exit code $LASTEXITCODE"
    }

    New-Item -ItemType Directory -Force -Path $artifactRoot | Out-Null
    Copy-Item -LiteralPath $kioskApk -Destination $kioskArtifact -Force
    Copy-Item -LiteralPath $webPocApk -Destination $webPocArtifact -Force
    $checksumLines = @(
        "$((Get-FileHash -LiteralPath $kioskArtifact -Algorithm SHA256).Hash)  $([IO.Path]::GetFileName($kioskArtifact))",
        "$((Get-FileHash -LiteralPath $webPocArtifact -Algorithm SHA256).Hash)  $([IO.Path]::GetFileName($webPocArtifact))"
    )
    [System.IO.File]::WriteAllLines($checksumFile, $checksumLines)
} finally {
    foreach ($name in @(
        'MATHOLIC_RELEASE_STORE_FILE',
        'MATHOLIC_RELEASE_STORE_PASSWORD',
        'MATHOLIC_RELEASE_KEY_ALIAS',
        'MATHOLIC_RELEASE_KEY_PASSWORD'
    )) {
        Remove-Item "Env:$name" -ErrorAction SilentlyContinue
    }
    $password = $null
}

Write-Host "Kiosk release APK: $kioskArtifact"
Write-Host "Web POC release APK: $webPocArtifact"
Write-Host "Checksums: $checksumFile"
