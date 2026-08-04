param(
    [string]$PythonPath = (
        Join-Path $env:LOCALAPPDATA 'CodexBuild\matholic-receiver-venv\Scripts\python.exe'
    )
)

$ErrorActionPreference = 'Stop'
$sourceRoot = $PSScriptRoot
$repositoryRoot = Split-Path $sourceRoot -Parent
$buildRoot = Join-Path $env:LOCALAPPDATA 'CodexBuild\matholic-receiver-package'
$workPath = Join-Path $buildRoot 'work'
$distPath = Join-Path $buildRoot 'dist'
$executable = Join-Path $distPath 'MatholicPdfReceiver.exe'
$artifact = Join-Path $repositoryRoot 'artifacts\matholic-pdf-receiver-0.1.6.exe'
$checksum = Join-Path $repositoryRoot 'artifacts\PC_RECEIVER_SHA256.txt'

if (-not (Test-Path -LiteralPath $PythonPath)) {
    throw "Receiver Python environment not found: $PythonPath"
}

$previousLocation = Get-Location
try {
    Set-Location -LiteralPath $sourceRoot
    $env:PYTHONPATH = (Resolve-Path '.\src').Path
    & $PythonPath -m pytest '.\tests' -q
    if ($LASTEXITCODE -ne 0) {
        throw 'PC receiver tests failed.'
    }
    & $PythonPath -m PyInstaller `
        --clean `
        --noconfirm `
        --workpath $workPath `
        --distpath $distPath `
        '.\MatholicPdfReceiver.spec'
    if ($LASTEXITCODE -ne 0) {
        throw 'PC receiver packaging failed.'
    }
    $smoke = Start-Process `
        -FilePath $executable `
        -ArgumentList '--smoke-check' `
        -WindowStyle Hidden `
        -PassThru `
        -Wait
    if ($smoke.ExitCode -ne 0) {
        throw "PC receiver smoke check failed: $($smoke.ExitCode)"
    }
    Copy-Item -LiteralPath $executable -Destination $artifact -Force
    $hash = (Get-FileHash -Algorithm SHA256 -LiteralPath $artifact).Hash
    Set-Content -LiteralPath $checksum -Encoding ascii -Value (
        "$hash  matholic-pdf-receiver-0.1.6.exe"
    )
    Write-Output "PC receiver artifact: $artifact"
    Write-Output "SHA-256: $hash"
} finally {
    Set-Location -LiteralPath $previousLocation
}
