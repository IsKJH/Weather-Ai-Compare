$projectDir = "C:\proj\weather-ai-compare\projects\weather-codex"
$logFile    = "C:\proj\weather-ai-compare\logs\v9_codex.log"
$metaFile   = "C:\proj\weather-ai-compare\logs\v9_codex_meta.json"
$prompt     = Get-Content "C:\proj\weather-ai-compare\prompt_v9_codex.txt" -Raw

Set-Location $projectDir

$start = Get-Date
Write-Host "[Codex V9] 시작: $start"

codex exec --dangerously-bypass-approvals-and-sandbox $prompt 2>&1 | Tee-Object -FilePath $logFile

$end = Get-Date
$elapsed = ($end - $start).TotalSeconds
Write-Host "[Codex V9] 종료: $end (소요: $([math]::Round($elapsed,1)) 초)"

$meta = @{
    ai        = "codex"
    phase     = "v9"
    startTime = $start.ToString("o")
    endTime   = $end.ToString("o")
    elapsedSec = [math]::Round($elapsed, 1)
}
$meta | ConvertTo-Json | Out-File $metaFile -Encoding utf8
Write-Host "메타 저장: $metaFile"
