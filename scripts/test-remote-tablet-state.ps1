$ErrorActionPreference = 'Stop'

. (Join-Path $PSScriptRoot 'remote-tablet-state.ps1')

function Assert-Equal {
    param(
        [Parameter(Mandatory = $true)]
        [object]$Expected,
        [Parameter(Mandatory = $true)]
        [object]$Actual,
        [Parameter(Mandatory = $true)]
        [string]$Message
    )

    if ($Expected -ne $Actual) {
        throw "$Message Expected=[$Expected] Actual=[$Actual]"
    }
}

function Assert-Throws {
    param(
        [Parameter(Mandatory = $true)]
        [scriptblock]$Action,
        [Parameter(Mandatory = $true)]
        [string]$Pattern
    )

    try {
        & $Action
    } catch {
        if ($_.Exception.Message -notmatch $Pattern) {
            throw "Unexpected exception: $($_.Exception.Message)"
        }
        return
    }
    throw "Expected exception matching: $Pattern"
}

$targets = @(
    @{ Component = 'kiosk' },
    @{ Component = 'web' }
)

$calls = [System.Collections.Generic.List[string]]::new()
Set-RemoteSupportTargets -Enabled $true -Targets $targets -SendCommand {
    param($target, $enabled)
    $calls.Add("$($target.Component):$enabled") | Out-Null
}
Assert-Equal `
    -Expected 'kiosk:True,web:True' `
    -Actual ($calls -join ',') `
    -Message 'Healthy enable did not update both targets in order.'

$calls.Clear()
Assert-Throws -Pattern 'Remote support enable failed' -Action {
    Set-RemoteSupportTargets -Enabled $true -Targets $targets -SendCommand {
        param($target, $enabled)
        $calls.Add("$($target.Component):$enabled") | Out-Null
        if ($target.Component -eq 'web' -and $enabled) {
            throw 'synthetic web enable failure'
        }
    }
}
Assert-Equal `
    -Expected 'kiosk:True,web:True,kiosk:False,web:False' `
    -Actual ($calls -join ',') `
    -Message 'Partial enable did not roll back every target.'

$calls.Clear()
Assert-Throws -Pattern 'Remote support disable failed' -Action {
    Set-RemoteSupportTargets -Enabled $false -Targets $targets -SendCommand {
        param($target, $enabled)
        $calls.Add("$($target.Component):$enabled") | Out-Null
        if ($target.Component -eq 'kiosk') {
            throw 'synthetic kiosk disable failure'
        }
    }
}
Assert-Equal `
    -Expected 'kiosk:False,web:False' `
    -Actual ($calls -join ',') `
    -Message 'Disable failure prevented the remaining target from being attempted.'

$calls.Clear()
Assert-Throws -Pattern 'Fail-closed rollback also failed' -Action {
    Set-RemoteSupportTargets -Enabled $true -Targets $targets -SendCommand {
        param($target, $enabled)
        $calls.Add("$($target.Component):$enabled") | Out-Null
        if ($target.Component -eq 'web') {
            throw "synthetic web $enabled failure"
        }
    }
}
Assert-Equal `
    -Expected 'kiosk:True,web:True,kiosk:False,web:False' `
    -Actual ($calls -join ',') `
    -Message 'Rollback failure did not preserve all attempted operations.'

Write-Output 'REMOTE_TABLET_STATE_TESTS=PASS'
