[CmdletBinding()]
param(
    [string]$Serial,
    [string]$ExpectedModel = 'SM-P610',
    [ValidateSet('', 'PASSED', 'FAILED', 'ABORTED')]
    [string]$ExpectedStatus = '',
    [string]$ExpectedReason = '',
    [int]$ExpectedCompleted = -1,
    [int]$StartTimeoutSeconds = 120,
    [int]$RunTimeoutSeconds = 5400
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
        Target = Read-Int 'gate3_target'
        State = Read-String 'state'
        Reason = Read-String 'reason'
    }
}

Write-Host "Armed on $($device.Model); waiting for Gate 3 RUNNING."
$startDeadline = [DateTimeOffset]::UtcNow.AddSeconds($StartTimeoutSeconds)
do {
    Start-Sleep -Milliseconds 500
    $snapshot = Read-Gate3State
} while ($snapshot.Status -ne 'RUNNING' -and [DateTimeOffset]::UtcNow -lt $startDeadline)

if ($snapshot.Status -ne 'RUNNING') {
    throw "Gate 3 did not enter RUNNING; status=$($snapshot.Status), state=$($snapshot.State)."
}

$lastCompleted = $snapshot.Completed
Write-Host "RUNNING $lastCompleted/$($snapshot.Target)"
$runDeadline = [DateTimeOffset]::UtcNow.AddSeconds($RunTimeoutSeconds)
while ([DateTimeOffset]::UtcNow -lt $runDeadline) {
    Start-Sleep -Seconds 1
    $snapshot = Read-Gate3State
    if ($snapshot.Completed -ge 0 -and $snapshot.Completed -ne $lastCompleted) {
        $lastCompleted = $snapshot.Completed
        Write-Host "PROGRESS $lastCompleted/$($snapshot.Target)"
    }
    if ($snapshot.Status -in @('PASSED', 'FAILED', 'ABORTED')) {
        Write-Host "FINAL status=$($snapshot.Status) completed=$($snapshot.Completed)/$($snapshot.Target) state=$($snapshot.State) reason=$($snapshot.Reason)"
        if ($ExpectedStatus -and $snapshot.Status -ne $ExpectedStatus) {
            throw "Expected status $ExpectedStatus, found $($snapshot.Status)."
        }
        if ($ExpectedReason -and $snapshot.Reason -ne $ExpectedReason) {
            throw "Expected reason $ExpectedReason, found $($snapshot.Reason)."
        }
        if ($ExpectedCompleted -ge 0 -and $snapshot.Completed -ne $ExpectedCompleted) {
            throw "Expected completed $ExpectedCompleted, found $($snapshot.Completed)."
        }
        return
    }
}

throw "Gate 3 did not reach a terminal status within $RunTimeoutSeconds seconds."
