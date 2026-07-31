param(
    [string]$Serial = 'R54TB029FHZ',

    [switch]$NoWaitForAcknowledgement
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

. (Join-Path $PSScriptRoot 'admin-pin-store.ps1')

$firstPin = $null
$secondPin = $null

function Wait-ForAcknowledgement {
    param([string]$Message)

    if (-not $NoWaitForAcknowledgement) {
        Write-Host ''
        [void](Read-Host $Message)
    }
}

try {
    try {
        $Host.UI.RawUI.WindowTitle = '매쓰홀릭 관리자 PIN 임시 보관'
    } catch { }

    Clear-Host
    Write-Host '매쓰홀릭 관리자 PIN 임시 보관' -ForegroundColor Cyan
    Write-Host 'PIN은 Windows DPAPI로 암호화되며 저장소와 Git에 들어가지 않습니다.'
    Write-Host '보관 파일을 삭제하면 자동 PIN 입력 권한이 즉시 중단됩니다.'
    Write-Host ''
    $firstPin = Read-Host '6~12자리 숫자 PIN' -AsSecureString
    $secondPin = Read-Host 'PIN 확인' -AsSecureString
    if (-not (Test-MatholicSecurePinsEqual -First $firstPin -Second $secondPin)) {
        throw '두 PIN이 일치하지 않습니다. 아무 것도 저장하지 않았습니다.'
    }

    $credentialPath = Get-MatholicAdminPinCredentialPath
    Protect-MatholicAdminPin `
        -SecurePin $firstPin `
        -Serial $Serial `
        -Path $credentialPath

    Write-Host ''
    Write-Host '암호화 PIN 보관을 완료했습니다.' -ForegroundColor Green
    Write-Host "보관 위치: $credentialPath"
    Write-Output 'STORED_ADMIN_PIN=READY'
    Wait-ForAcknowledgement -Message '이 창을 닫으려면 Enter'
} catch {
    Write-Host ''
    Write-Host $_.Exception.Message -ForegroundColor Red
    Wait-ForAcknowledgement -Message '이 창을 닫으려면 Enter'
    exit 1
} finally {
    if ($null -ne $firstPin) {
        $firstPin.Dispose()
    }
    if ($null -ne $secondPin) {
        $secondPin.Dispose()
    }
}
