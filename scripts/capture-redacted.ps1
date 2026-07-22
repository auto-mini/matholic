[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
$adb = 'C:\Users\user\AppData\Local\Android\Sdk\platform-tools\adb.exe'
$probePackage = 'com.local.matholickiosk.probe.debug'
$captureAction = 'com.local.matholickiosk.probe.action.CAPTURE_REDACTED'

if (-not (Test-Path -LiteralPath $adb)) { throw "adb not found: $adb" }

$enabled = & $adb shell settings get secure enabled_accessibility_services
if ($enabled -notmatch [regex]::Escape($probePackage)) {
    throw 'Probe accessibility service is not enabled.'
}

$result = & $adb shell run-as $probePackage am broadcast --user 0 -a $captureAction -p $probePackage 2>&1
if ($LASTEXITCODE -ne 0 -or $result -match 'Exception|SecurityException|Error') {
    throw "Private capture request failed: $($result -join ' ')"
}

Start-Sleep -Milliseconds 500
$listing = & $adb shell run-as $probePackage ls files/redacted_reports 2>&1
$reports = @($listing | Where-Object { $_ -match '\.json$' })
if ($reports.Count -eq 0) {
    throw 'No redacted report was created. Keep Matholic in the foreground and retry.'
}

Write-Host "Private capture request completed. Redacted report count: $($reports.Count)"
