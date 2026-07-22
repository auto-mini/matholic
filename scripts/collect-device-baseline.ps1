[CmdletBinding()]
param(
    [string]$Serial,
    [string]$ExpectedModel = 'SM-P610'
)

$ErrorActionPreference = 'Stop'
$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
. (Join-Path $PSScriptRoot 'webpoc-device-common.ps1')
$adb = Get-WebPocAdbPath
$outputDirectory = Join-Path $projectRoot 'artifacts'
$output = Join-Path $outputDirectory 'device_a_baseline.txt'

$device = Resolve-WebPocDevice -AdbPath $adb -Serial $Serial -ExpectedModel $ExpectedModel
New-Item -ItemType Directory -Force -Path $outputDirectory | Out-Null

$packageFacts = & $adb -s $device.Serial shell dumpsys package com.matholic.mathapp |
    Select-String -Pattern 'versionCode=|versionName=|minSdk=|targetSdk=' |
    Select-Object -First 6 |
    ForEach-Object { $_.Line.Trim() }

$facts = @(
    'redacted=true'
    'serialStored=false'
    "capturedAt=$([DateTimeOffset]::UtcNow.ToString('o'))"
    "model=$($device.Model)"
    "android=$($device.Android)"
    "sdk=$($device.Sdk)"
    "oneUi=$(& $adb -s $device.Serial shell getprop ro.build.version.oneui)"
    "wmSize=$(& $adb -s $device.Serial shell wm size | Out-String)".Trim()
    "wmDensity=$(& $adb -s $device.Serial shell wm density | Out-String)".Trim()
    "fontScale=$(& $adb -s $device.Serial shell settings get system font_scale)"
    "targetPackage=com.matholic.mathapp"
    $packageFacts
)

$facts | Set-Content -LiteralPath $output -Encoding utf8
Write-Host "Saved non-sensitive baseline: $output"
Write-Host 'No serial, accounts, screen content, logcat, clipboard, or UI dump was collected.'
