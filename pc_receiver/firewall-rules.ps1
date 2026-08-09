function Remove-MatholicReceiverInboundFirewallRules {
    param(
        [Parameter(Mandatory = $true)]
        [string[]]$ExecutablePaths
    )

    $seenPaths = [Collections.Generic.HashSet[string]]::new(
        [StringComparer]::OrdinalIgnoreCase
    )
    foreach ($path in $ExecutablePaths) {
        if ([string]::IsNullOrWhiteSpace($path)) {
            continue
        }
        $normalizedPath = [IO.Path]::GetFullPath($path)
        if (-not $seenPaths.Add($normalizedPath)) {
            continue
        }
        Get-NetFirewallApplicationFilter `
            -Program $normalizedPath `
            -ErrorAction SilentlyContinue |
            Get-NetFirewallRule -ErrorAction SilentlyContinue |
            Where-Object Direction -eq 'Inbound' |
            Remove-NetFirewallRule | Out-Null
    }
}

function Assert-MatholicReceiverPrivateFirewallRule {
    param(
        [Parameter(Mandatory = $true)]
        [string]$DisplayName,
        [Parameter(Mandatory = $true)]
        [string]$ExecutablePath
    )

    $rules = @(Get-NetFirewallRule -DisplayName $DisplayName -ErrorAction SilentlyContinue)
    if ($rules.Count -ne 1) {
        throw "Expected exactly one receiver firewall rule, found $($rules.Count)."
    }
    $rule = $rules[0]
    $application = $rule | Get-NetFirewallApplicationFilter
    $port = $rule | Get-NetFirewallPortFilter
    $expectedPath = [IO.Path]::GetFullPath($ExecutablePath)
    $actualPath = [IO.Path]::GetFullPath($application.Program)
    $localPorts = @($port.LocalPort | ForEach-Object { "$_" })
    $protocol = "$($port.Protocol)"

    if (
        $rule.Enabled -ne 'True' -or
        $rule.Direction -ne 'Inbound' -or
        $rule.Action -ne 'Allow' -or
        "$($rule.Profile)" -ne 'Private' -or
        -not $actualPath.Equals($expectedPath, [StringComparison]::OrdinalIgnoreCase) -or
        $protocol -notin @('TCP', '6') -or
        $localPorts -notcontains '48129'
    ) {
        throw 'Receiver firewall rule does not match Private/TCP/48129/exact-program policy.'
    }
}
