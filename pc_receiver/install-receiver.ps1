param(
    [Parameter(Mandatory = $true)]
    [string]$ExecutablePath
)

$ErrorActionPreference = 'Stop'
$resolvedExecutable = (Resolve-Path -LiteralPath $ExecutablePath).Path
$installRoot = Join-Path $env:LOCALAPPDATA 'MatholicPdfReceiver\app'
$targetExecutable = Join-Path $installRoot 'MatholicPdfReceiver.exe'
$startupFolder = [Environment]::GetFolderPath('Startup')
$startupShortcut = Join-Path $startupFolder 'Matholic PDF Receiver.lnk'
$firewallRuleName = 'Matholic PDF Receiver (Private)'
. (Join-Path $PSScriptRoot 'firewall-rules.ps1')

New-Item -ItemType Directory -Path $installRoot -Force | Out-Null
Copy-Item -LiteralPath $resolvedExecutable -Destination $targetExecutable -Force

$shell = New-Object -ComObject WScript.Shell
$shortcut = $shell.CreateShortcut($startupShortcut)
$shortcut.TargetPath = $targetExecutable
$shortcut.Arguments = '--background'
$shortcut.WorkingDirectory = $installRoot
$shortcut.Description = '매쓰홀릭 키오스크 PDF 전용 수신기'
$shortcut.Save()

Remove-MatholicReceiverInboundFirewallRules -ExecutablePaths @(
    $resolvedExecutable,
    $targetExecutable
)
Get-NetFirewallRule -DisplayName $firewallRuleName -ErrorAction SilentlyContinue |
    Remove-NetFirewallRule
New-NetFirewallRule `
    -DisplayName $firewallRuleName `
    -Direction Inbound `
    -Action Allow `
    -Profile Private `
    -Program $targetExecutable `
    -Protocol TCP `
    -LocalPort 48129 | Out-Null
Assert-MatholicReceiverPrivateFirewallRule `
    -DisplayName $firewallRuleName `
    -ExecutablePath $targetExecutable

Write-Output $targetExecutable
