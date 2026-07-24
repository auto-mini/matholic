[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [string]$BackupDirectory,
    [string]$SigningRoot = (Join-Path $env:LOCALAPPDATA 'MatholicKiosk\release-signing')
)

$ErrorActionPreference = 'Stop'
$javaRoot = 'C:\Users\user\AppData\Local\Android\jdks\jdk-17.0.19+10'
$keytool = Join-Path $javaRoot 'bin\keytool.exe'
$keystorePath = Join-Path $SigningRoot 'matholic-kiosk-release.p12'
$credentialPath = Join-Path $SigningRoot 'matholic-kiosk-release.credential.clixml'
$markerPath = Join-Path $SigningRoot 'portable-recovery-confirmed.json'

foreach ($path in @($keytool, $keystorePath, $credentialPath)) {
    if (-not (Test-Path -LiteralPath $path)) {
        throw "Required signing input not found: $path"
    }
}

New-Item -ItemType Directory -Force -Path $BackupDirectory | Out-Null
$resolvedBackupDirectory = (Resolve-Path -LiteralPath $BackupDirectory).Path
$resolvedSigningRoot = (Resolve-Path -LiteralPath $SigningRoot).Path
if ($resolvedBackupDirectory.StartsWith($resolvedSigningRoot, [StringComparison]::OrdinalIgnoreCase)) {
    throw 'Portable backup directory must be outside the local signing directory.'
}

$credential = Import-Clixml -LiteralPath $credentialPath
if ($credential -isnot [pscredential]) {
    throw "Invalid DPAPI signing credential: $credentialPath"
}
$password = $credential.GetNetworkCredential().Password
$backupPath = Join-Path $resolvedBackupDirectory 'matholic-kiosk-release.p12'
$certificatePath = Join-Path $env:TEMP "matholic-release-$([guid]::NewGuid().ToString('N')).cer"
$confirmed = $false

try {
    Copy-Item -LiteralPath $keystorePath -Destination $backupPath -Force
    $sourceHash = (Get-FileHash -LiteralPath $keystorePath -Algorithm SHA256).Hash
    $backupHash = (Get-FileHash -LiteralPath $backupPath -Algorithm SHA256).Hash
    if ($sourceHash -ne $backupHash) {
        throw 'Portable keystore backup hash does not match the source.'
    }

    $env:MATHOLIC_KEYTOOL_PASSWORD = $password
    & $keytool `
        -exportcert `
        -alias $credential.UserName `
        -keystore $keystorePath `
        -storetype PKCS12 `
        -storepass:env MATHOLIC_KEYTOOL_PASSWORD `
        -file $certificatePath
    if ($LASTEXITCODE -ne 0) {
        throw "Certificate export failed with exit code $LASTEXITCODE"
    }
    $signerSha256 = (Get-FileHash -LiteralPath $certificatePath -Algorithm SHA256).Hash

    Add-Type -AssemblyName System.Windows.Forms
    Add-Type -AssemblyName System.Drawing
    $form = [System.Windows.Forms.Form]::new()
    $form.Text = '매쓰홀릭 release 서명키 복구 확인'
    $form.Width = 760
    $form.Height = 340
    $form.StartPosition = 'CenterScreen'
    $form.TopMost = $true

    $label = [System.Windows.Forms.Label]::new()
    $label.Left = 20
    $label.Top = 20
    $label.Width = 700
    $label.Height = 70
    $label.Text = "아래 복구 비밀번호를 별도 비밀번호 관리자 또는 오프라인 기록에 보관하세요.`r`n키 파일과 비밀번호를 같은 장소에만 두지 마세요. 이 창의 값은 Git이나 로그에 저장되지 않습니다."
    $form.Controls.Add($label)

    $passwordBox = [System.Windows.Forms.TextBox]::new()
    $passwordBox.Left = 20
    $passwordBox.Top = 100
    $passwordBox.Width = 700
    $passwordBox.ReadOnly = $true
    $passwordBox.Text = $password
    $form.Controls.Add($passwordBox)

    $copyButton = [System.Windows.Forms.Button]::new()
    $copyButton.Left = 20
    $copyButton.Top = 145
    $copyButton.Width = 160
    $copyButton.Text = '클립보드로 복사'
    $copyButton.Add_Click({ [System.Windows.Forms.Clipboard]::SetText($password) })
    $form.Controls.Add($copyButton)

    $checkBox = [System.Windows.Forms.CheckBox]::new()
    $checkBox.Left = 20
    $checkBox.Top = 195
    $checkBox.Width = 700
    $checkBox.Text = '키 파일과 별도로 복구 비밀번호를 안전하게 보관했습니다.'
    $form.Controls.Add($checkBox)

    $confirmButton = [System.Windows.Forms.Button]::new()
    $confirmButton.Left = 500
    $confirmButton.Top = 235
    $confirmButton.Width = 220
    $confirmButton.Text = '복구 준비 완료'
    $confirmButton.Enabled = $false
    $confirmButton.DialogResult = [System.Windows.Forms.DialogResult]::OK
    $checkBox.Add_CheckedChanged({ $confirmButton.Enabled = $checkBox.Checked })
    $form.Controls.Add($confirmButton)
    $form.AcceptButton = $confirmButton

    $dialogResult = $form.ShowDialog()
    $confirmed = $dialogResult -eq [System.Windows.Forms.DialogResult]::OK
    if (-not $confirmed) {
        throw 'Portable signing recovery confirmation was cancelled.'
    }

    $marker = [ordered]@{
        confirmedAt = [DateTimeOffset]::Now.ToString('o')
        backupPath = $backupPath
        keystoreSha256 = $sourceHash
        signerSha256 = $signerSha256
    }
    [System.IO.File]::WriteAllText(
        $markerPath,
        ($marker | ConvertTo-Json),
        [System.Text.UTF8Encoding]::new($false)
    )
} catch {
    if (-not $confirmed -and (Test-Path -LiteralPath $backupPath)) {
        [System.IO.File]::Delete($backupPath)
    }
    throw
} finally {
    Remove-Item Env:MATHOLIC_KEYTOOL_PASSWORD -ErrorAction SilentlyContinue
    if (Test-Path -LiteralPath $certificatePath) {
        [System.IO.File]::Delete($certificatePath)
    }
    try {
        if (
            [System.Windows.Forms.Clipboard]::ContainsText() -and
            [System.Windows.Forms.Clipboard]::GetText() -eq $password
        ) {
            [System.Windows.Forms.Clipboard]::Clear()
        }
    } catch {
        # Clipboard cleanup is best-effort after the local confirmation dialog closes.
    }
    $password = $null
}

Write-Host "Portable keystore backup verified: $backupPath"
Write-Host "Recovery confirmation marker: $markerPath"
Write-Host 'Release signer is eligible for production provisioning.'
