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
$kioskArtifact = Join-Path $artifactRoot 'matholic-kiosk-0.6.0-rc54-release.apk'
$webPocArtifact = Join-Path $artifactRoot 'matholic-webpoc-0.4.0-rc109-release.apk'
$checksumFile = Join-Path $artifactRoot 'RELEASE_SHA256SUMS.txt'

foreach ($path in @($javaRoot, $sdkRoot, $keystorePath, $credentialPath)) {
    if (-not (Test-Path -LiteralPath $path)) {
        throw "Required release build input not found: $path"
    }
}

function ConvertTo-Hex([byte[]]$Bytes) {
    return [System.BitConverter]::ToString($Bytes).Replace('-', '')
}

function Get-ApkPayloadFingerprint([string]$ApkPath) {
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    $archive = [System.IO.Compression.ZipFile]::OpenRead($ApkPath)
    $records = [System.Collections.Generic.List[string]]::new()
    try {
        foreach (
            $entry in $archive.Entries |
                Where-Object { $_.FullName -ne 'META-INF/version-control-info.textproto' } |
                Sort-Object FullName
        ) {
            $stream = $entry.Open()
            $sha256 = [System.Security.Cryptography.SHA256]::Create()
            try {
                $digest = ConvertTo-Hex ($sha256.ComputeHash($stream))
            } finally {
                $sha256.Dispose()
                $stream.Dispose()
            }
            [void]$records.Add("$($entry.FullName)`0$($entry.Length)`0$digest")
        }
    } finally {
        $archive.Dispose()
    }
    $payload = [Text.Encoding]::UTF8.GetBytes([string]::Join("`n", $records))
    $payloadSha256 = [System.Security.Cryptography.SHA256]::Create()
    try {
        return ConvertTo-Hex ($payloadSha256.ComputeHash($payload))
    } finally {
        [Array]::Clear($payload, 0, $payload.Length)
        $payloadSha256.Dispose()
    }
}

function Publish-VersionedArtifact(
    [string]$Source,
    [string]$Destination,
    [string]$Label
) {
    if (-not (Test-Path -LiteralPath $Destination)) {
        Copy-Item -LiteralPath $Source -Destination $Destination
        return
    }
    $sourceFingerprint = Get-ApkPayloadFingerprint $Source
    $destinationFingerprint = Get-ApkPayloadFingerprint $Destination
    if ($sourceFingerprint -ne $destinationFingerprint) {
        throw "$Label artifact already exists with different payload; bump its version before publishing"
    }
    Write-Host "$Label artifact payload is unchanged; preserving the existing versioned APK."
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
    Publish-VersionedArtifact $kioskApk $kioskArtifact 'Kiosk'
    Publish-VersionedArtifact $webPocApk $webPocArtifact 'Web POC'
    & (Join-Path $PSScriptRoot 'verify-release-apks.ps1') `
        -KioskApk $kioskArtifact `
        -WebPocApk $webPocArtifact
    if ($LASTEXITCODE -ne 0) {
        throw "Stored release APK verification failed with exit code $LASTEXITCODE"
    }
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
