function Get-WebPocAdbPath {
    param(
        [string]$SdkRoot = 'C:\Users\user\AppData\Local\Android\Sdk'
    )

    $adbPath = Join-Path $SdkRoot 'platform-tools\adb.exe'
    if (-not (Test-Path -LiteralPath $adbPath)) {
        throw "adb not found: $adbPath"
    }
    return $adbPath
}

function Resolve-WebPocDevice {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)]
        [string]$AdbPath,

        [string]$Serial,

        [string]$ExpectedModel
    )

    $deviceOutput = @(& $AdbPath devices)
    if ($LASTEXITCODE -ne 0) {
        throw "adb devices failed with exit code $LASTEXITCODE"
    }

    $records = @(
        $deviceOutput |
            Select-Object -Skip 1 |
            ForEach-Object {
                if ($_ -match '^([^\s]+)\s+([^\s]+)$') {
                    [pscustomobject]@{
                        Serial = $Matches[1]
                        State = $Matches[2]
                    }
                }
            }
    )
    $authorized = @($records | Where-Object { $_.State -eq 'device' })

    if ($Serial) {
        $selected = @($authorized | Where-Object { $_.Serial -eq $Serial })
        if ($selected.Count -ne 1) {
            throw 'The requested device is not connected and authorized.'
        }
        $target = $selected[0]
    } elseif ($ExpectedModel) {
        $modelMatches = @(
            foreach ($candidate in $authorized) {
                $candidateModel = (@(& $AdbPath -s $candidate.Serial shell getprop ro.product.model) -join '').Trim()
                if ($LASTEXITCODE -ne 0) {
                    throw "Failed to read the model of an authorized device (exit $LASTEXITCODE)."
                }
                if ($candidateModel -eq $ExpectedModel) {
                    $candidate
                }
            }
        )
        if ($modelMatches.Count -ne 1) {
            throw "Expected exactly one authorized $ExpectedModel device; found $($modelMatches.Count)."
        }
        $target = $modelMatches[0]
    } else {
        if ($authorized.Count -ne 1) {
            throw "Expected exactly one authorized device; found $($authorized.Count). Use -Serial or -ExpectedModel."
        }
        $target = $authorized[0]
    }

    $model = (@(& $AdbPath -s $target.Serial shell getprop ro.product.model) -join '').Trim()
    $android = (@(& $AdbPath -s $target.Serial shell getprop ro.build.version.release) -join '').Trim()
    $sdk = (@(& $AdbPath -s $target.Serial shell getprop ro.build.version.sdk) -join '').Trim()
    if ($LASTEXITCODE -ne 0) {
        throw "Failed to read target device properties (exit $LASTEXITCODE)."
    }
    if ($ExpectedModel -and $model -ne $ExpectedModel) {
        throw "Wrong device model: expected $ExpectedModel, found $model."
    }

    return [pscustomobject]@{
        Serial = $target.Serial
        Model = $model
        Android = $android
        Sdk = $sdk
    }
}

function Wait-WebPocIdle {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)]
        [string]$AdbPath,

        [Parameter(Mandatory)]
        [string]$Serial,

        [int]$TimeoutSeconds = 45
    )

    $deadline = [DateTimeOffset]::UtcNow.AddSeconds($TimeoutSeconds)
    $lastState = 'UNKNOWN'
    while ([DateTimeOffset]::UtcNow -lt $deadline) {
        $stateXml = (@(
            & $AdbPath -s $Serial exec-out run-as com.local.matholickiosk.webpoc `
                cat shared_prefs/web_poc_state.xml 2>$null
        ) -join "`n")
        if ($stateXml -match '<string name="state">([^<]+)</string>') {
            $lastState = $Matches[1]
            if ($lastState -eq 'IDLE') {
                Write-Host 'Web POC reached IDLE.'
                return
            }
            if ($lastState -in @('LOCKED', 'MAINTENANCE_REQUIRED')) {
                throw "Web POC reached terminal state $lastState instead of IDLE."
            }
        }
        Start-Sleep -Milliseconds 250
    }

    throw "Web POC did not reach IDLE within $TimeoutSeconds seconds; last state: $lastState."
}
