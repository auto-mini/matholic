[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [string]$KioskApk,
    [Parameter(Mandatory = $true)]
    [string]$WebPocApk,
    [string]$ExpectedKioskVersion = '0.5.0-rc02',
    [string]$ExpectedWebPocVersion = '0.3.5-rc02'
)

$ErrorActionPreference = 'Stop'
$javaRoot = 'C:\Users\user\AppData\Local\Android\jdks\jdk-17.0.19+10'
$sdkRoot = 'C:\Users\user\AppData\Local\Android\Sdk'
$apksigner = Join-Path $sdkRoot 'build-tools\37.0.0\apksigner.bat'
$zipalign = Join-Path $sdkRoot 'build-tools\37.0.0\zipalign.exe'
$apkanalyzer = Join-Path $sdkRoot 'cmdline-tools\latest\bin\apkanalyzer.bat'
$debugSignerSha256 = '0b6bef1c18a3beb397b655e895d30412aa749b712fd801c26a9b9e386e8579f8'

$env:JAVA_HOME = $javaRoot
foreach ($path in @($KioskApk, $WebPocApk, $apksigner, $zipalign, $apkanalyzer)) {
    if (-not (Test-Path -LiteralPath $path)) {
        throw "Required file not found: $path"
    }
}
$verificationParent = Join-Path $env:LOCALAPPDATA 'MatholicKiosk\verification'
$verificationRoot = Join-Path $verificationParent ([guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Force -Path $verificationRoot | Out-Null
$stagedKioskApk = Join-Path $verificationRoot 'kiosk-release.apk'
$stagedWebPocApk = Join-Path $verificationRoot 'webpoc-release.apk'
Copy-Item -LiteralPath $KioskApk -Destination $stagedKioskApk
Copy-Item -LiteralPath $WebPocApk -Destination $stagedWebPocApk

function Get-ApkSignerDigest([string]$ApkPath) {
    $output = (& $apksigner verify --verbose --print-certs $ApkPath 2>&1) -join "`n"
    if ($LASTEXITCODE -ne 0) {
        throw "APK signature verification failed: $ApkPath"
    }
    if ($output -notmatch 'Verified using v2 scheme \(APK Signature Scheme v2\): true') {
        throw "APK Signature Scheme v2 is required: $ApkPath"
    }
    if ($output -notmatch 'Number of signers: 1') {
        throw "Exactly one APK signer is required: $ApkPath"
    }
    $digest = [regex]::Match(
        $output,
        'certificate SHA-256 digest:\s*([0-9a-fA-F]{64})'
    ).Groups[1].Value.ToLowerInvariant()
    if (-not $digest) {
        throw "Could not read signer digest: $ApkPath"
    }
    if ($digest -eq $debugSignerSha256 -or $output -match 'CN=Android Debug') {
        throw "Debug signer is forbidden for release APK: $ApkPath"
    }
    return $digest
}

function Assert-ApkManifest(
    [string]$ApkPath,
    [string]$ExpectedPackage,
    [string]$ExpectedVersion,
    [string[]]$RequiredPermissions,
    [string[]]$ForbiddenPermissions
) {
    $packageName = ((& $apkanalyzer manifest application-id $ApkPath) -join '').Trim()
    $versionName = ((& $apkanalyzer manifest version-name $ApkPath) -join '').Trim()
    $debuggable = ((& $apkanalyzer manifest debuggable $ApkPath) -join '').Trim()
    $permissions = @(& $apkanalyzer manifest permissions $ApkPath)
    if ($packageName -ne $ExpectedPackage) {
        throw "Unexpected applicationId in ${ApkPath}: $packageName"
    }
    if ($versionName -ne $ExpectedVersion) {
        throw "Unexpected versionName in ${ApkPath}: $versionName"
    }
    if ($debuggable -ne 'false') {
        throw "Release APK must not be debuggable: $ApkPath"
    }
    foreach ($permission in $RequiredPermissions) {
        if ($permission -notin $permissions) {
            throw "Required permission missing from ${ApkPath}: $permission"
        }
    }
    foreach ($permission in $ForbiddenPermissions) {
        if ($permission -in $permissions) {
            throw "Forbidden permission found in ${ApkPath}: $permission"
        }
    }
    & $zipalign -c -P 16 4 $ApkPath | Out-Null
    if ($LASTEXITCODE -ne 0) {
        throw "zipalign verification failed: $ApkPath"
    }
}

try {
    $kioskDigest = Get-ApkSignerDigest $stagedKioskApk
    $webPocDigest = Get-ApkSignerDigest $stagedWebPocApk
    if ($kioskDigest -ne $webPocDigest) {
        throw 'Kiosk and Web POC APK signer digests do not match.'
    }

    Assert-ApkManifest `
        -ApkPath $stagedKioskApk `
        -ExpectedPackage 'com.local.matholickiosk.kiosk' `
        -ExpectedVersion $ExpectedKioskVersion `
        -RequiredPermissions @('android.permission.CAMERA') `
        -ForbiddenPermissions @('android.permission.INTERNET', 'android.permission.ACCESS_NETWORK_STATE')
    Assert-ApkManifest `
        -ApkPath $stagedWebPocApk `
        -ExpectedPackage 'com.local.matholickiosk.webpoc' `
        -ExpectedVersion $ExpectedWebPocVersion `
        -RequiredPermissions @(
            'android.permission.INTERNET',
            'com.local.matholickiosk.permission.CREDENTIAL_BRIDGE'
        ) `
        -ForbiddenPermissions @()

    Write-Host "Release APK verification passed."
    Write-Host "Signer SHA-256: $kioskDigest"
} finally {
    foreach ($path in @($stagedKioskApk, $stagedWebPocApk)) {
        if (Test-Path -LiteralPath $path) {
            [System.IO.File]::Delete($path)
        }
    }
    if (Test-Path -LiteralPath $verificationRoot) {
        [System.IO.Directory]::Delete($verificationRoot, $false)
    }
}
