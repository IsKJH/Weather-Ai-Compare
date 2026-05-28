param(
    [string]$Root = "C:\proj\weather-ai-compare"
)

$ErrorActionPreference = "Stop"

$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
$androidStudioJbr = "C:\Users\mojis\AppData\Local\Programs\Android Studio\jbr"
$androidSdk = "$env:LOCALAPPDATA\Android\Sdk"

if (-not $env:JAVA_HOME -and (Test-Path $androidStudioJbr)) {
    $env:JAVA_HOME = $androidStudioJbr
    $env:Path = "$env:JAVA_HOME\bin;$env:Path"
}

if (-not $env:ANDROID_HOME -and (Test-Path $androidSdk)) {
    $env:ANDROID_HOME = $androidSdk
    $env:ANDROID_SDK_ROOT = $androidSdk
}

if (-not (Test-Path $adb)) {
    throw "adb not found: $adb"
}

$phases = @(
    @{ Phase = "v1"; WorkRoot = "C:\proj\weather-ai-compare-v1-capture" },
    @{ Phase = "v2"; WorkRoot = "C:\proj\weather-ai-compare-v2-capture" },
    @{ Phase = "v3"; WorkRoot = $Root }
)

$ais = @("claude", "codex", "gemini")
$packageName = "com.example.weathernow"

function Capture-DeviceScreen {
    param(
        [string]$Phase,
        [string]$Ai,
        [string]$Name
    )
    $outDir = Join-Path $Root "screenshots\$Phase\$Ai"
    New-Item -ItemType Directory -Force -Path $outDir | Out-Null
    $devicePath = "/sdcard/weather_${Phase}_${Ai}_${Name}.png"
    $localPath = Join-Path $outDir "$Name.png"
    & $adb shell screencap -p $devicePath | Out-Host
    & $adb pull $devicePath $localPath | Out-Host
    & $adb shell rm $devicePath | Out-Host
    if (-not (Test-Path $localPath)) {
        throw "screenshot failed: $localPath"
    }
    Write-Host "saved $localPath"
}

foreach ($phaseInfo in $phases) {
    $phase = $phaseInfo.Phase
    $workRoot = $phaseInfo.WorkRoot

    foreach ($ai in $ais) {
        $projectDir = Join-Path $workRoot "projects\weather-$ai"
        $apkPath = Join-Path $projectDir "app\build\outputs\apk\debug\app-debug.apk"

        Write-Host "`n[$phase/$ai] build"
        Push-Location $projectDir
        try {
            & ".\gradlew.bat" assembleDebug | Out-Host
        } finally {
            Pop-Location
        }

        if (-not (Test-Path $apkPath)) {
            throw "APK not found: $apkPath"
        }

        Write-Host "[$phase/$ai] install and capture"
        & $adb install -r $apkPath | Out-Host
        & $adb shell am force-stop $packageName | Out-Host
        & $adb shell pm clear $packageName | Out-Host
        & $adb shell am start -n "$packageName/.MainActivity" | Out-Host
        Start-Sleep -Seconds 3

        # Reset scroll position to top for scrollable Compose screens.
        & $adb shell input swipe 500 520 500 1850 600 | Out-Host
        Start-Sleep -Milliseconds 700
        & $adb shell input swipe 500 520 500 1850 600 | Out-Host
        Start-Sleep -Milliseconds 700
        Capture-DeviceScreen -Phase $phase -Ai $ai -Name "01-top"

        & $adb shell input swipe 500 1750 500 520 700 | Out-Host
        Start-Sleep -Milliseconds 900
        Capture-DeviceScreen -Phase $phase -Ai $ai -Name "02-middle"

        & $adb shell input swipe 500 1750 500 520 700 | Out-Host
        Start-Sleep -Milliseconds 900
        Capture-DeviceScreen -Phase $phase -Ai $ai -Name "03-bottom"

        & $adb shell am force-stop $packageName | Out-Host
    }
}

Write-Host "`n[capture complete]"
