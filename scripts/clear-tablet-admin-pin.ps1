param()

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

. (Join-Path $PSScriptRoot 'admin-pin-store.ps1')

$credentialPath = Get-MatholicAdminPinCredentialPath
if ([IO.File]::Exists($credentialPath)) {
    [IO.File]::Delete($credentialPath)
    Write-Output 'STORED_ADMIN_PIN=REMOVED'
} else {
    Write-Output 'STORED_ADMIN_PIN=ABSENT'
}
