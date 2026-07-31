param(
    [Parameter(Mandatory = $true)]
    [ValidateSet('Start', 'Capture', 'Stop')]
    [string]$Action,

    [string]$Serial = 'R54TB029FHZ',

    [ValidateRange(1, 120)]
    [int]$Minutes = 60,

    [string]$OutputPath = (
        Join-Path $env:LOCALAPPDATA 'MatholicRemote\A-latest.png'
    )
)

$ErrorActionPreference = 'Stop'

$adb = Join-Path $env:LOCALAPPDATA 'Android\Sdk\platform-tools\adb.exe'
if (-not (Test-Path -LiteralPath $adb)) {
    throw "ADB not found: $adb"
}

$deviceState = ((& $adb -s $Serial get-state 2>&1) -join '').Trim()
if ($LASTEXITCODE -ne 0 -or $deviceState -ne 'device') {
    throw "Tablet is not available as an authorized ADB device: $Serial ($deviceState)"
}

$model = ((& $adb -s $Serial shell getprop ro.product.model 2>&1) -join '').Trim()
if ($LASTEXITCODE -ne 0 -or $model -ne 'SM-P610') {
    throw "Unexpected tablet model for ${Serial}: $model"
}

function Set-RemoteSupport {
    param(
        [bool]$Enabled
    )

    $enabledText = if ($Enabled) { 'true' } else { 'false' }
    $durationSeconds = $Minutes * 60
    $targets = @(
        @{
            Action = 'com.local.matholickiosk.kiosk.action.SET_REMOTE_SUPPORT'
            Component = 'com.local.matholickiosk.kiosk/.AdbRemoteSupportReceiver'
        },
        @{
            Action = 'com.local.matholickiosk.webpoc.action.SET_REMOTE_SUPPORT'
            Component = 'com.local.matholickiosk.webpoc/.AdbRemoteSupportReceiver'
        }
    )

    foreach ($target in $targets) {
        $output = & $adb -s $Serial shell am broadcast `
            -a $target.Action `
            -n $target.Component `
            --ez enabled $enabledText `
            --ei duration_seconds $durationSeconds 2>&1
        if ($LASTEXITCODE -ne 0 -or ($output -join "`n") -notmatch 'result=0') {
            throw "Remote support command failed for $($target.Component): $($output -join ' ')"
        }
    }
}

function Capture-Screen {
    $resolvedOutput = [System.IO.Path]::GetFullPath($OutputPath)
    $outputDirectory = Split-Path -Parent $resolvedOutput
    [System.IO.Directory]::CreateDirectory($outputDirectory) | Out-Null
    $remotePath = '/data/local/tmp/matholic-remote-screen.png'

    try {
        & $adb -s $Serial shell screencap -p $remotePath
        if ($LASTEXITCODE -ne 0) {
            throw 'Tablet rejected the screenshot. Start remote support first.'
        }
        & $adb -s $Serial pull $remotePath $resolvedOutput | Out-Null
        if ($LASTEXITCODE -ne 0) {
            throw 'Failed to copy the tablet screenshot to the PC.'
        }
    } finally {
        & $adb -s $Serial shell rm -f $remotePath 2>$null
    }

    $file = Get-Item -LiteralPath $resolvedOutput
    if ($file.Length -lt 8) {
        throw 'Screenshot is empty. Remote support may have expired.'
    }
    $signature = [System.IO.File]::ReadAllBytes($resolvedOutput)[0..7]
    $expected = [byte[]](137, 80, 78, 71, 13, 10, 26, 10)
    if (-not [System.Linq.Enumerable]::SequenceEqual[byte]($signature, $expected)) {
        throw 'Captured file is not a valid PNG.'
    }
    return $file
}

switch ($Action) {
    'Start' {
        Set-RemoteSupport -Enabled $true
        Start-Sleep -Milliseconds 500
        $capture = Capture-Screen
        Write-Output "REMOTE_SUPPORT=ACTIVE"
        Write-Output "EXPIRES_IN_MINUTES=$Minutes"
        Write-Output "SCREENSHOT=$($capture.FullName)"
    }
    'Capture' {
        $capture = Capture-Screen
        Write-Output "SCREENSHOT=$($capture.FullName)"
    }
    'Stop' {
        Set-RemoteSupport -Enabled $false
        if (Test-Path -LiteralPath $OutputPath) {
            Remove-Item -LiteralPath $OutputPath -Force
        }
        Write-Output 'REMOTE_SUPPORT=INACTIVE'
    }
}
