[CmdletBinding()]
param(
    [string]$SigningRoot = (Join-Path $env:LOCALAPPDATA 'MatholicKiosk\release-signing'),
    [string]$Alias = 'matholic-kiosk-release'
)

$ErrorActionPreference = 'Stop'
$javaRoot = 'C:\Users\user\AppData\Local\Android\jdks\jdk-17.0.19+10'
$keytool = Join-Path $javaRoot 'bin\keytool.exe'
$keystorePath = Join-Path $SigningRoot 'matholic-kiosk-release.p12'
$credentialPath = Join-Path $SigningRoot 'matholic-kiosk-release.credential.clixml'

if (-not (Test-Path -LiteralPath $keytool)) {
    throw "keytool not found: $keytool"
}
if (
    (Test-Path -LiteralPath $keystorePath) -or
    (Test-Path -LiteralPath $credentialPath)
) {
    throw "Release signing material already exists. Refusing to overwrite: $SigningRoot"
}

New-Item -ItemType Directory -Force -Path $SigningRoot | Out-Null
$createdKeystore = $false
try {
    $randomBytes = [byte[]]::new(32)
    [System.Security.Cryptography.RandomNumberGenerator]::Fill($randomBytes)
    $password = [Convert]::ToBase64String($randomBytes)
    $password = $password.TrimEnd('=')
    $password = $password.Replace('+', '-').Replace('/', '_')
    $env:MATHOLIC_KEYTOOL_PASSWORD = $password

    & $keytool `
        -genkeypair `
        -alias $Alias `
        -keyalg RSA `
        -keysize 4096 `
        -sigalg SHA256withRSA `
        -validity 10000 `
        -dname 'CN=Matholic Kiosk Release, O=auto-mini, C=KR' `
        -keystore $keystorePath `
        -storetype PKCS12 `
        -storepass:env MATHOLIC_KEYTOOL_PASSWORD `
        -keypass:env MATHOLIC_KEYTOOL_PASSWORD `
        -noprompt
    if ($LASTEXITCODE -ne 0) {
        throw "keytool failed with exit code $LASTEXITCODE"
    }
    $createdKeystore = $true

    $securePassword = ConvertTo-SecureString -String $password -AsPlainText -Force
    [pscredential]::new($Alias, $securePassword) |
        Export-Clixml -LiteralPath $credentialPath

    $identity = [System.Security.Principal.WindowsIdentity]::GetCurrent().Name
    foreach ($path in @($keystorePath, $credentialPath)) {
        $acl = [System.Security.AccessControl.FileSecurity]::new()
        $acl.SetAccessRuleProtection($true, $false)
        $rule = [System.Security.AccessControl.FileSystemAccessRule]::new(
            $identity,
            [System.Security.AccessControl.FileSystemRights]::FullControl,
            [System.Security.AccessControl.AccessControlType]::Allow
        )
        $acl.AddAccessRule($rule)
        Set-Acl -LiteralPath $path -AclObject $acl
    }
} catch {
    if ($createdKeystore -and -not (Test-Path -LiteralPath $credentialPath)) {
        [System.IO.File]::Delete($keystorePath)
    }
    throw
} finally {
    Remove-Item Env:MATHOLIC_KEYTOOL_PASSWORD -ErrorAction SilentlyContinue
    if ($randomBytes) {
        [Array]::Clear($randomBytes, 0, $randomBytes.Length)
    }
    $password = $null
}

Write-Host "Release keystore created: $keystorePath"
Write-Host "Windows DPAPI credential created: $credentialPath"
Write-Warning 'Portable recovery backup is NOT complete. Do not deploy this signer until the keystore and its recovery password are stored separately.'
