$projectDir = "C:\proj\weather-ai-compare\projects\weather-gemini"
$logFile    = "C:\proj\weather-ai-compare\logs\v9_gemini.log"
$metaFile   = "C:\proj\weather-ai-compare\logs\v9_gemini_meta.json"
$prompt     = Get-Content "C:\proj\weather-ai-compare\prompt_v9_gemini.txt" -Raw

Set-Location $projectDir

$start = Get-Date
Write-Host "[Gemini V9] 시작: $start"

gemini -p $prompt --yolo 2>&1 | Tee-Object -FilePath $logFile

$end = Get-Date
$elapsed = ($end - $start).TotalSeconds
Write-Host "[Gemini V9] 종료: $end (소요: $([math]::Round($elapsed,1)) 초)"

$meta = @{
    ai        = "gemini"
    phase     = "v9"
    startTime = $start.ToString("o")
    endTime   = $end.ToString("o")
    elapsedSec = [math]::Round($elapsed, 1)
}
$meta | ConvertTo-Json | Out-File $metaFile -Encoding utf8
Write-Host "메타 저장: $metaFile"
