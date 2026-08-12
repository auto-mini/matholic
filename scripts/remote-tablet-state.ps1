function Get-RemoteSupportTargetLabel {
    param(
        [Parameter(Mandatory = $true)]
        [object]$Target
    )

    $component = $Target.Component
    if ([string]::IsNullOrWhiteSpace($component)) {
        return $Target.ToString()
    }
    return $component
}

function Set-RemoteSupportTargets {
    param(
        [Parameter(Mandatory = $true)]
        [bool]$Enabled,

        [Parameter(Mandatory = $true)]
        [object[]]$Targets,

        [Parameter(Mandatory = $true)]
        [scriptblock]$SendCommand
    )

    if ($Targets.Count -eq 0) {
        throw 'At least one remote support target is required.'
    }

    $operation = if ($Enabled) { 'enable' } else { 'disable' }
    $failures = [System.Collections.Generic.List[string]]::new()
    foreach ($target in $Targets) {
        try {
            $null = & $SendCommand $target $Enabled
        } catch {
            $label = Get-RemoteSupportTargetLabel -Target $target
            $failures.Add("${label}: $($_.Exception.Message)")
            if ($Enabled) {
                break
            }
        }
    }

    if ($failures.Count -eq 0) {
        return
    }

    if (-not $Enabled) {
        throw "Remote support disable failed: $($failures -join '; ')"
    }

    $rollbackFailures = [System.Collections.Generic.List[string]]::new()
    foreach ($target in $Targets) {
        try {
            $null = & $SendCommand $target $false
        } catch {
            $label = Get-RemoteSupportTargetLabel -Target $target
            $rollbackFailures.Add("${label}: $($_.Exception.Message)")
        }
    }

    $message = "Remote support $operation failed: $($failures -join '; ')"
    if ($rollbackFailures.Count -gt 0) {
        $message += ". Fail-closed rollback also failed: " +
            ($rollbackFailures -join '; ')
    }
    throw $message
}
