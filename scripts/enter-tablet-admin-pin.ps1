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
$uiDumpPath = '/sdcard/Download/matholic-admin-pin-ui.xml'

function Wait-ForAcknowledgement {
    param([string]$Message)

    Write-Host ''
    [void](Read-Host $Message)
}

function Get-VisibleUiNode {
    param(
        [string]$ResourceId,
        [string]$ClassName,
        [string]$Text,
        [Nullable[bool]]$Password
    )

    try {
        & $adb -s $Serial shell uiautomator dump $uiDumpPath *> $null
        if ($LASTEXITCODE -ne 0) {
            throw 'A 기기 화면 구조를 확인하지 못했습니다.'
        }
        $xmlText = (& $adb -s $Serial exec-out cat $uiDumpPath 2>$null) -join "`n"
        if (
            $LASTEXITCODE -ne 0 -or
            [string]::IsNullOrWhiteSpace($xmlText) -or
            $xmlText -notmatch '^<\?xml'
        ) {
            throw 'A 기기 화면 구조를 읽지 못했습니다.'
        }
        [xml]$document = $xmlText
        return @($document.SelectNodes('//node')) |
            Where-Object {
                $_.GetAttribute('visible-to-user') -ne 'false' -and
                (
                    [string]::IsNullOrEmpty($ResourceId) -or
                    $_.GetAttribute('resource-id') -eq $ResourceId
                ) -and
                (
                    [string]::IsNullOrEmpty($ClassName) -or
                    $_.GetAttribute('class') -eq $ClassName
                ) -and
                (
                    [string]::IsNullOrEmpty($Text) -or
                    $_.GetAttribute('text') -eq $Text
                ) -and
                (
                    $null -eq $Password -or
                    $_.GetAttribute('password') -eq $Password.ToString().ToLowerInvariant()
                )
            } |
            Select-Object -First 1
    } finally {
        & $adb -s $Serial shell rm -f $uiDumpPath 2>$null | Out-Null
    }
}

function Get-UiNodeCenter {
    param(
        [Parameter(Mandatory = $true)]
        $Node
    )

    $bounds = $Node.GetAttribute('bounds')
    $match = [regex]::Match($bounds, '^\[(\d+),(\d+)\]\[(\d+),(\d+)\]$')
    if (-not $match.Success) {
        throw 'A 기기 입력 위치를 확인하지 못했습니다.'
    }
    return @{
        X = [int](
            ([int]$match.Groups[1].Value + [int]$match.Groups[3].Value) / 2
        )
        Y = [int](
            ([int]$match.Groups[2].Value + [int]$match.Groups[4].Value) / 2
        )
    }
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
    $pinNode = Get-VisibleUiNode -ResourceId 'com.local.matholickiosk.kiosk:id/pin_input'
    if ($null -eq $pinNode) {
        $pinNode = Get-VisibleUiNode `
            -ClassName 'android.widget.EditText' `
            -Password $true
    }
    $submitNode = Get-VisibleUiNode -ResourceId 'com.local.matholickiosk.kiosk:id/auth_submit'
    if ($null -eq $submitNode) {
        $submitNode = Get-VisibleUiNode -ResourceId 'android:id/button1'
    }
    if ($null -eq $submitNode) {
        $submitNode = Get-VisibleUiNode -ClassName 'android.widget.Button' -Text '인증'
    }
    if ($null -eq $pinNode -or $null -eq $submitNode) {
        throw '현재 화면은 관리자 PIN 입력 화면이 아닙니다.'
    }
    if ($submitNode.GetAttribute('enabled') -ne 'true') {
        throw '관리자 인증 버튼이 아직 활성화되지 않았습니다.'
    }
    $pinCenter = Get-UiNodeCenter -Node $pinNode
    $submitCenter = Get-UiNodeCenter -Node $submitNode

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
    $shell.StandardInput.WriteLine("input tap $($pinCenter.X) $($pinCenter.Y)")
    $shell.StandardInput.WriteLine('input keyevent KEYCODE_MOVE_END')
    1..12 | ForEach-Object { $shell.StandardInput.WriteLine('input keyevent KEYCODE_DEL') }
    foreach ($digitByte in $pinBytes) {
        $digit = [char]$digitByte
        $shell.StandardInput.WriteLine("input keyevent KEYCODE_$digit")
    }
    $shell.StandardInput.WriteLine('sleep 0.2')
    $shell.StandardInput.WriteLine('input keyevent KEYCODE_BACK')
    $shell.StandardInput.WriteLine('sleep 0.2')
    $shell.StandardInput.WriteLine("input tap $($submitCenter.X) $($submitCenter.Y)")
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
