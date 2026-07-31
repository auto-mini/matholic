param(
    [string]$Serial = 'R54TB029FHZ',

    [switch]$RequireStoredPin
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

. (Join-Path $PSScriptRoot 'admin-pin-store.ps1')

$adb = Join-Path $env:LOCALAPPDATA 'Android\Sdk\platform-tools\adb.exe'
$securePin = $null
$pinBytes = $null
$shell = $null
$credentialPath = Get-MatholicAdminPinCredentialPath
$useStoredPin = [IO.File]::Exists($credentialPath)
$nonInteractive = $RequireStoredPin -or $useStoredPin

function Wait-ForAcknowledgement {
    param([string]$Message)

    Write-Host ''
    [void](Read-Host $Message)
}

try {
    try {
        $Host.UI.RawUI.WindowTitle = '매쓰홀릭 A 기기 관리자 PIN 전송'
    } catch { }
    if (-not (Test-Path -LiteralPath $adb)) {
        throw "ADB를 찾을 수 없습니다: $adb"
    }
    if ($Serial -notmatch '^[A-Za-z0-9._:-]+$') {
        throw '올바르지 않은 기기 일련번호입니다.'
    }

    $deviceState = ((& $adb -s $Serial get-state 2>&1) -join '').Trim()
    if ($LASTEXITCODE -ne 0 -or $deviceState -ne 'device') {
        throw "A 기기가 USB 디버깅으로 연결되지 않았습니다. ($deviceState)"
    }
    $model = ((& $adb -s $Serial shell getprop ro.product.model 2>&1) -join '').Trim()
    if ($LASTEXITCODE -ne 0 -or $model -ne 'SM-P610') {
        throw "예상한 A 기기가 아닙니다: $model"
    }
    $resumed = ((& $adb -s $Serial shell dumpsys activity activities 2>&1) -join "`n")
    if ($LASTEXITCODE -ne 0 -or $resumed -notmatch 'com\.local\.matholickiosk\.kiosk/\.MainActivity') {
        throw '키오스크 관리자 PIN 화면을 확인할 수 없습니다.'
    }

    if ($useStoredPin) {
        [byte[]]$pinBytes = Unprotect-MatholicAdminPin `
            -Serial $Serial `
            -Path $credentialPath
    } else {
        if ($RequireStoredPin) {
            throw '저장된 관리자 PIN 파일이 없어 자동 입력을 중단했습니다.'
        }
        Clear-Host
        Write-Host '매쓰홀릭 A 기기 관리자 PIN 전송' -ForegroundColor Cyan
        Write-Host 'PIN은 화면에 표시되거나 파일·로그로 저장되지 않습니다.'
        Write-Host '잘못 입력했다면 Enter를 누르기 전 Ctrl+C로 취소하세요.'
        Write-Host ''
        $securePin = Read-Host '6~12자리 숫자 PIN' -AsSecureString
        [byte[]]$pinBytes = ConvertFrom-MatholicSecurePin `
            -SecurePin $securePin
    }

    $startInfo = [Diagnostics.ProcessStartInfo]::new()
    $startInfo.FileName = $adb
    $startInfo.Arguments = "-s $Serial shell"
    $startInfo.UseShellExecute = $false
    $startInfo.CreateNoWindow = $true
    $startInfo.RedirectStandardInput = $true
    $startInfo.RedirectStandardOutput = $true
    $startInfo.RedirectStandardError = $true
    $shell = [Diagnostics.Process]::new()
    $shell.StartInfo = $startInfo
    if (-not $shell.Start()) {
        throw 'ADB 입력 채널을 열지 못했습니다.'
    }

    # Keep the PIN out of process arguments and logs. Commands travel only through
    # the already-authorized ADB shell stdin, one key event at a time.
    $shell.StandardInput.WriteLine('input keyevent KEYCODE_BACK')
    $shell.StandardInput.WriteLine('input tap 1000 625')
    $shell.StandardInput.WriteLine('input keyevent KEYCODE_MOVE_END')
    1..12 | ForEach-Object { $shell.StandardInput.WriteLine('input keyevent KEYCODE_DEL') }
    foreach ($digitByte in $pinBytes) {
        $digit = [char]$digitByte
        $shell.StandardInput.WriteLine("input keyevent KEYCODE_$digit")
    }
    $shell.StandardInput.WriteLine('sleep 0.2')
    $shell.StandardInput.WriteLine('input keyevent KEYCODE_BACK')
    $shell.StandardInput.WriteLine('sleep 0.2')
    $shell.StandardInput.WriteLine('input tap 1000 772')
    $shell.StandardInput.WriteLine('exit')
    $shell.StandardInput.Close()
    if (-not $shell.WaitForExit(10000)) {
        try { $shell.Kill() } catch { }
        throw 'A 기기가 PIN 입력에 응답하지 않았습니다.'
    }
    if ($shell.ExitCode -ne 0) {
        $errorText = $shell.StandardError.ReadToEnd().Trim()
        throw "ADB PIN 전송이 실패했습니다. $errorText"
    }

    Write-Host ''
    Write-Host 'PIN을 A 기기로 전송했습니다.' -ForegroundColor Green
    if ($useStoredPin) {
        Write-Output 'PIN_SOURCE=DPAPI_FILE'
        Write-Output 'PIN_SENT=TRUE'
    } else {
        Write-Host 'PIN 값은 저장하지 않았습니다.'
        Wait-ForAcknowledgement -Message '이 창을 닫으려면 Enter'
    }
} catch {
    Write-Host ''
    Write-Host $_.Exception.Message -ForegroundColor Red
    if (-not $nonInteractive) {
        Wait-ForAcknowledgement -Message '이 창을 닫으려면 Enter'
    }
    exit 1
} finally {
    if ($null -ne $pinBytes) {
        [Array]::Clear($pinBytes, 0, $pinBytes.Length)
    }
    if ($null -ne $securePin) {
        $securePin.Dispose()
    }
    if ($null -ne $shell) {
        $shell.Dispose()
    }
}
