$adb         = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
$screenshotDir = "C:\proj\weather-ai-compare\screenshots"
$projects    = @("claude", "codex", "gemini")

foreach ($ai in $projects) {
    $projectDir = "C:\proj\weather-ai-compare\projects\weather-$ai"
    $apkPath    = "$projectDir\app\build\outputs\apk\debug\app-debug.apk"

    Write-Host "`n[$ai] 빌드 시작..."
    Set-Location $projectDir

    # Gradle 빌드
    $buildResult = & ".\gradlew.bat" assembleDebug 2>&1
    if ($LASTEXITCODE -ne 0) {
        Write-Host "[$ai] 빌드 실패 — 스킵"
        # 메타에 빌드 실패 기록
        $metaFile = "C:\proj\weather-ai-compare\logs\${ai}_meta.json"
        if (Test-Path $metaFile) {
            $meta = Get-Content $metaFile | ConvertFrom-Json
            $meta | Add-Member -NotePropertyName buildSuccess -NotePropertyValue $false -Force
            $meta | ConvertTo-Json | Out-File $metaFile -Encoding utf8
        }
        continue
    }
    Write-Host "[$ai] 빌드 성공"

    # APK 설치
    Write-Host "[$ai] APK 설치 중..."
    & $adb install -r $apkPath

    # 앱 실행 (패키지명은 build.gradle의 applicationId 기준)
    $packageName = "com.example.weathernow"
    & $adb shell am start -n "$packageName/.MainActivity"
    Start-Sleep -Seconds 3

    # 스크린샷
    $devicePath = "/sdcard/weather_${ai}.png"
    $localPath  = "$screenshotDir\${ai}.png"
    & $adb shell screencap -p $devicePath
    & $adb pull $devicePath $localPath
    & $adb shell rm $devicePath
    Write-Host "[$ai] 스크린샷 저장: $localPath"

    # 메타에 빌드 성공 기록
    $metaFile = "C:\proj\weather-ai-compare\logs\${ai}_meta.json"
    if (Test-Path $metaFile) {
        $meta = Get-Content $metaFile | ConvertFrom-Json
        $meta | Add-Member -NotePropertyName buildSuccess -NotePropertyValue $true -Force
        $meta | ConvertTo-Json | Out-File $metaFile -Encoding utf8
    }

    # 앱 종료
    & $adb shell am force-stop $packageName
    Start-Sleep -Seconds 1
}

Write-Host "`n[완료] 스크린샷이 $screenshotDir 에 저장됐습니다."
