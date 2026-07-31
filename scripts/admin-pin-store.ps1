Set-StrictMode -Version Latest

$script:MatholicAdminPinMagic = [Text.Encoding]::ASCII.GetBytes("MAPIN1`0")

function Get-MatholicAdminPinCredentialPath {
    Join-Path $env:LOCALAPPDATA 'MatholicRemote\admin-pin.dpapi'
}

function Get-MatholicAdminPinEntropy {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Serial
    )

    $binding = [Text.Encoding]::UTF8.GetBytes(
        "MatholicKiosk.AdminPin.v1`0$Serial"
    )
    try {
        return [Security.Cryptography.SHA256]::HashData($binding)
    } finally {
        [Array]::Clear($binding, 0, $binding.Length)
    }
}

function ConvertFrom-MatholicSecurePin {
    param(
        [Parameter(Mandatory = $true)]
        [Security.SecureString]$SecurePin
    )

    $pointer = [IntPtr]::Zero
    $pinBytes = $null
    try {
        $pointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR(
            $SecurePin
        )
        $byteLength = [Runtime.InteropServices.Marshal]::ReadInt32(
            $pointer,
            -4
        )
        if (($byteLength % 2) -ne 0) {
            throw 'PIN 메모리 형식이 올바르지 않습니다.'
        }
        $pinLength = [int]($byteLength / 2)
        if ($pinLength -lt 6 -or $pinLength -gt 12) {
            throw 'PIN은 6~12자리 숫자여야 합니다.'
        }
        $pinBytes = [byte[]]::new($pinLength)
        for ($index = 0; $index -lt $pinLength; $index += 1) {
            $character = [Runtime.InteropServices.Marshal]::ReadInt16(
                $pointer,
                $index * 2
            )
            if ($character -lt 48 -or $character -gt 57) {
                throw 'PIN은 6~12자리 숫자여야 합니다.'
            }
            $pinBytes[$index] = [byte]$character
        }
        return ,$pinBytes
    } catch {
        if ($null -ne $pinBytes) {
            [Array]::Clear($pinBytes, 0, $pinBytes.Length)
        }
        throw
    } finally {
        if ($pointer -ne [IntPtr]::Zero) {
            [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($pointer)
        }
    }
}

function Test-MatholicSecurePinsEqual {
    param(
        [Parameter(Mandatory = $true)]
        [Security.SecureString]$First,

        [Parameter(Mandatory = $true)]
        [Security.SecureString]$Second
    )

    [byte[]]$firstBytes = ConvertFrom-MatholicSecurePin -SecurePin $First
    [byte[]]$secondBytes = ConvertFrom-MatholicSecurePin -SecurePin $Second
    try {
        if ($firstBytes.Length -ne $secondBytes.Length) {
            return $false
        }
        $difference = 0
        for ($index = 0; $index -lt $firstBytes.Length; $index += 1) {
            $difference = $difference -bor (
                $firstBytes[$index] -bxor $secondBytes[$index]
            )
        }
        return $difference -eq 0
    } finally {
        [Array]::Clear($firstBytes, 0, $firstBytes.Length)
        [Array]::Clear($secondBytes, 0, $secondBytes.Length)
    }
}

function Protect-MatholicAdminPin {
    param(
        [Parameter(Mandatory = $true)]
        [Security.SecureString]$SecurePin,

        [Parameter(Mandatory = $true)]
        [string]$Serial,

        [Parameter(Mandatory = $true)]
        [string]$Path
    )

    [byte[]]$pinBytes = ConvertFrom-MatholicSecurePin -SecurePin $SecurePin
    [byte[]]$entropy = Get-MatholicAdminPinEntropy -Serial $Serial
    $protectedBytes = $null
    $payload = $null
    $temporaryPath = $null
    try {
        $protectedBytes = [Security.Cryptography.ProtectedData]::Protect(
            $pinBytes,
            $entropy,
            [Security.Cryptography.DataProtectionScope]::CurrentUser
        )
        $payload = [byte[]]::new(
            $script:MatholicAdminPinMagic.Length + $protectedBytes.Length
        )
        [Array]::Copy(
            $script:MatholicAdminPinMagic,
            0,
            $payload,
            0,
            $script:MatholicAdminPinMagic.Length
        )
        [Array]::Copy(
            $protectedBytes,
            0,
            $payload,
            $script:MatholicAdminPinMagic.Length,
            $protectedBytes.Length
        )

        $resolvedPath = [IO.Path]::GetFullPath($Path)
        $directory = Split-Path -Parent $resolvedPath
        [IO.Directory]::CreateDirectory($directory) | Out-Null
        $temporaryPath = Join-Path $directory (
            '.admin-pin.' + [Guid]::NewGuid().ToString('N') + '.tmp'
        )
        [IO.File]::WriteAllBytes($temporaryPath, $payload)
        [IO.File]::Move($temporaryPath, $resolvedPath, $true)
        $temporaryPath = $null
    } finally {
        [Array]::Clear($pinBytes, 0, $pinBytes.Length)
        [Array]::Clear($entropy, 0, $entropy.Length)
        if ($null -ne $protectedBytes) {
            [Array]::Clear($protectedBytes, 0, $protectedBytes.Length)
        }
        if ($null -ne $payload) {
            [Array]::Clear($payload, 0, $payload.Length)
        }
        if ($null -ne $temporaryPath -and [IO.File]::Exists($temporaryPath)) {
            [IO.File]::Delete($temporaryPath)
        }
    }
}

function Unprotect-MatholicAdminPin {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Serial,

        [Parameter(Mandatory = $true)]
        [string]$Path
    )

    $resolvedPath = [IO.Path]::GetFullPath($Path)
    if (-not [IO.File]::Exists($resolvedPath)) {
        throw '저장된 관리자 PIN 파일이 없습니다.'
    }

    $payload = [IO.File]::ReadAllBytes($resolvedPath)
    [byte[]]$entropy = Get-MatholicAdminPinEntropy -Serial $Serial
    $protectedBytes = $null
    $pinBytes = $null
    try {
        if ($payload.Length -le $script:MatholicAdminPinMagic.Length) {
            throw '저장된 관리자 PIN 파일이 손상되었습니다.'
        }
        for (
            $index = 0;
            $index -lt $script:MatholicAdminPinMagic.Length;
            $index += 1
        ) {
            if ($payload[$index] -ne $script:MatholicAdminPinMagic[$index]) {
                throw '저장된 관리자 PIN 파일 형식을 확인할 수 없습니다.'
            }
        }

        $protectedLength = (
            $payload.Length - $script:MatholicAdminPinMagic.Length
        )
        $protectedBytes = [byte[]]::new($protectedLength)
        [Array]::Copy(
            $payload,
            $script:MatholicAdminPinMagic.Length,
            $protectedBytes,
            0,
            $protectedLength
        )
        $pinBytes = [Security.Cryptography.ProtectedData]::Unprotect(
            $protectedBytes,
            $entropy,
            [Security.Cryptography.DataProtectionScope]::CurrentUser
        )
        if ($pinBytes.Length -lt 6 -or $pinBytes.Length -gt 12) {
            throw '저장된 관리자 PIN 길이가 올바르지 않습니다.'
        }
        foreach ($digit in $pinBytes) {
            if ($digit -lt 48 -or $digit -gt 57) {
                throw '저장된 관리자 PIN 형식이 올바르지 않습니다.'
            }
        }

        $result = $pinBytes
        $pinBytes = $null
        return ,$result
    } finally {
        [Array]::Clear($payload, 0, $payload.Length)
        [Array]::Clear($entropy, 0, $entropy.Length)
        if ($null -ne $protectedBytes) {
            [Array]::Clear($protectedBytes, 0, $protectedBytes.Length)
        }
        if ($null -ne $pinBytes) {
            [Array]::Clear($pinBytes, 0, $pinBytes.Length)
        }
    }
}
