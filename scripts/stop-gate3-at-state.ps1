[CmdletBinding()]
param(
    [Parameter(Mandatory)]
    [ValidateSet('LOGIN_SUBMIT', 'ACTIVE', 'LOGOUT_SUBMIT')]
    [string]$AtState,
    [string]$Serial,
    [string]$ExpectedModel = 'SM-P610',
    [int]$TimeoutSeconds = 180
)

$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'webpoc-device-common.ps1')
$adb = Get-WebPocAdbPath
$device = Resolve-WebPocDevice -AdbPath $adb -Serial $Serial -ExpectedModel $ExpectedModel

function Read-Gate3State {
    $xml = (@(
        & $adb -s $device.Serial exec-out run-as com.local.matholickiosk.webpoc `
            cat shared_prefs/web_poc_state.xml 2>$null
    ) -join "`n")
    function Read-String([string]$Name) {
        $match = [regex]::Match($xml, "<string name=`"$Name`">([^<]*)</string>")
        if ($match.Success) { return $match.Groups[1].Value }
        return ''
    }
    function Read-Int([string]$Name) {
        $match = [regex]::Match($xml, "<int name=`"$Name`" value=`"(\d+)`" />")
        if ($match.Success) { return [int]$match.Groups[1].Value }
        return -1
    }
    [pscustomobject]@{
        Status = Read-String 'gate3_status'
        Completed = Read-Int 'gate3_completed'
        State = Read-String 'state'
        Reason = Read-String 'reason'
    }
}

Write-Host "Armed on $($device.Model); waiting for $AtState."
$deadline = [DateTimeOffset]::UtcNow.AddSeconds($TimeoutSeconds)
do {
    Start-Sleep -Milliseconds 100
    $before = Read-Gate3State
    if ($before.Status -in @('FAILED', 'ABORTED', 'PASSED')) {
        throw "Gate 3 ended before $AtState; status=$($before.Status), state=$($before.State), reason=$($before.Reason)."
    }
} while (($before.Status -ne 'RUNNING' -or $before.State -ne $AtState) -and [DateTimeOffset]::UtcNow -lt $deadline)

if ($before.Status -ne 'RUNNING' -or $before.State -ne $AtState) {
    throw "Gate 3 did not reach $AtState within $TimeoutSeconds seconds."
}

$completedBeforeStop = $before.Completed
& $adb -s $device.Serial shell am force-stop com.local.matholickiosk.webpoc
if ($LASTEXITCODE -ne 0) { throw 'Failed to stop Web POC process.' }
Start-Sleep -Seconds 2
& $adb -s $device.Serial shell am start -n `
    'com.local.matholickiosk.webpoc/com.local.matholickiosk.webpoc.MainActivity' | Out-Null
if ($LASTEXITCODE -ne 0) { throw 'Failed to relaunch Web POC.' }

$recoveryDeadline = [DateTimeOffset]::UtcNow.AddSeconds(60)
do {
    Start-Sleep -Milliseconds 500
    $after = Read-Gate3State
} while (
    ($after.Status -ne 'ABORTED' -or $after.State -notin @('IDLE', 'LOCKED', 'MAINTENANCE_REQUIRED')) -and
    [DateTimeOffset]::UtcNow -lt $recoveryDeadline
)

Write-Host "FINAL status=$($after.Status) completed=$($after.Completed) state=$($after.State) reason=$($after.Reason)"
if ($after.Status -ne 'ABORTED') { throw "Expected ABORTED, found $($after.Status)." }
if ($after.Completed -ne $completedBeforeStop) {
    throw "Completed count changed across process stop: $completedBeforeStop -> $($after.Completed)."
}
if ($after.State -notin @('IDLE', 'LOCKED', 'MAINTENANCE_REQUIRED')) {
    throw "Recovery did not reach a safe state; state=$($after.State)."
}
