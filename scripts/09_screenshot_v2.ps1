$root = "C:\proj\weather-ai-compare"
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
$screenshotDir = Join-Path $root "screenshots\v2"
$projects = @("claude", "codex", "gemini")

New-Item -ItemType Directory -Force -Path $screenshotDir | Out-Null

foreach ($ai in $projects) {
    $projectDir = Join-Path $root "projects\weather-$ai"
    $apkPath = Join-Path $projectDir "app\build\outputs\apk\debug\app-debug.apk"
    $metaFile = Join-Path $root "logs\${ai}_v2_meta.json"

    Write-Host "`n[$ai v2] build start..."
    Set-Location $projectDir

    $buildOutput = & ".\gradlew.bat" assembleDebug 2>&1
    $buildSuccess = $LASTEXITCODE -eq 0

    if (Test-Path $metaFile) {
        $meta = Get-Content $metaFile | ConvertFrom-Json
    } else {
        $meta = [pscustomobject]@{ ai = $ai; phase = "v2" }
    }

    $meta | Add-Member -NotePropertyName buildSuccess -NotePropertyValue $buildSuccess -Force
    $meta | Add-Member -NotePropertyName buildCheckedAt -NotePropertyValue (Get-Date).ToString("o") -Force
    $meta | Add-Member -NotePropertyName buildOutputTail -NotePropertyValue (($buildOutput | Select-Object -Last 40) -join "`n") -Force

    if (-not $buildSuccess) {
        Write-Host "[$ai v2] build failed. Skipping install/screenshot."
        $meta | ConvertTo-Json -Depth 8 | Out-File $metaFile -Encoding utf8
        continue
    }

    Write-Host "[$ai v2] build passed"

    if (-not (Test-Path $adb)) {
        Write-Host "[$ai v2] adb not found at $adb. Skipping screenshot."
        $meta | Add-Member -NotePropertyName screenshotSuccess -NotePropertyValue $false -Force
        $meta | Add-Member -NotePropertyName screenshotNote -NotePropertyValue "adb not found" -Force
        $meta | ConvertTo-Json -Depth 8 | Out-File $metaFile -Encoding utf8
        continue
    }

    Write-Host "[$ai v2] installing APK..."
    & $adb install -r $apkPath | Out-Host

    $packageName = "com.example.weathernow"
    & $adb shell am start -n "$packageName/.MainActivity" | Out-Host
    Start-Sleep -Seconds 3

    $devicePath = "/sdcard/weather_${ai}_v2.png"
    $localPath = Join-Path $screenshotDir "${ai}.png"
    & $adb shell screencap -p $devicePath | Out-Host
    & $adb pull $devicePath $localPath | Out-Host
    & $adb shell rm $devicePath | Out-Host
    & $adb shell am force-stop $packageName | Out-Host

    $screenshotSuccess = Test-Path $localPath
    $meta | Add-Member -NotePropertyName screenshotSuccess -NotePropertyValue $screenshotSuccess -Force
    $meta | Add-Member -NotePropertyName screenshotPath -NotePropertyValue $localPath -Force
    $meta | ConvertTo-Json -Depth 8 | Out-File $metaFile -Encoding utf8

    Write-Host "[$ai v2] screenshot saved: $localPath"
    Start-Sleep -Seconds 1
}

Write-Host "`n[v2 complete] screenshots directory: $screenshotDir"
