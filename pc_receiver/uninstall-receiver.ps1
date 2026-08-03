$ErrorActionPreference = 'Stop'
$installRoot = Join-Path $env:LOCALAPPDATA 'MatholicPdfReceiver'
$startupFolder = [Environment]::GetFolderPath('Startup')
$startupShortcut = Join-Path $startupFolder 'Matholic PDF Receiver.lnk'
$firewallRuleName = 'Matholic PDF Receiver (Private)'

Get-Process -Name 'MatholicPdfReceiver' -ErrorAction SilentlyContinue |
    Stop-Process -Force
Get-NetFirewallRule -DisplayName $firewallRuleName -ErrorAction SilentlyContinue |
    Remove-NetFirewallRule
Remove-Item -LiteralPath $startupShortcut -Force -ErrorAction SilentlyContinue

Write-Output "수신 프로그램과 방화벽 규칙을 제거했습니다."
Write-Output "수신 PDF와 페어링 설정은 복구를 위해 보존했습니다: $installRoot"
