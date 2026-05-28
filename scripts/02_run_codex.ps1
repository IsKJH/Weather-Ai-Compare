$projectDir = "C:\proj\weather-ai-compare\projects\weather-codex"
$logFile    = "C:\proj\weather-ai-compare\logs\codex.log"
$metaFile   = "C:\proj\weather-ai-compare\logs\codex_meta.json"
$prompt     = Get-Content "C:\proj\weather-ai-compare\prompt.txt" -Raw

Set-Location $projectDir

$start = Get-Date
Write-Host "[Codex] 시작: $start"

codex exec --dangerously-bypass-approvals-and-sandbox $prompt 2>&1 | Tee-Object -FilePath $logFile

$end = Get-Date
$elapsed = ($end - $start).TotalSeconds
Write-Host "[Codex] 종료: $end (소요: $elapsed 초)"

# 토큰 파싱
$logContent = Get-Content $logFile -Raw
$inputTokens  = if ($logContent -match 'prompt_tokens["\s:]+([0-9]+)')      { $matches[1] } `
           elseif ($logContent -match 'input_tokens["\s:]+([0-9]+)')        { $matches[1] } `
           elseif ($logContent -match 'Input[:\s]+([0-9,]+)\s*tokens')      { $matches[1] -replace ',','' } `
           else { "N/A" }
$outputTokens = if ($logContent -match 'completion_tokens["\s:]+([0-9]+)')  { $matches[1] } `
           elseif ($logContent -match 'output_tokens["\s:]+([0-9]+)')       { $matches[1] } `
           elseif ($logContent -match 'Output[:\s]+([0-9,]+)\s*tokens')     { $matches[1] -replace ',','' } `
           else { "N/A" }

$ktFiles   = Get-ChildItem $projectDir -Recurse -Filter "*.kt" -ErrorAction SilentlyContinue
$lineCount = ($ktFiles | ForEach-Object { (Get-Content $_.FullName).Count } | Measure-Object -Sum).Sum
$fileCount = $ktFiles.Count

$meta = @{
    ai            = "codex"
    startTime     = $start.ToString("o")
    endTime       = $end.ToString("o")
    elapsedSec    = [math]::Round($elapsed, 1)
    inputTokens   = $inputTokens
    outputTokens  = $outputTokens
    ktFileCount   = $fileCount
    ktLineCount   = $lineCount
}
$meta | ConvertTo-Json | Out-File $metaFile -Encoding utf8

Write-Host "---"
Write-Host "소요 시간 : $elapsed 초"
Write-Host "입력 토큰 : $inputTokens"
Write-Host "출력 토큰 : $outputTokens"
Write-Host "KT 파일 수: $fileCount"
Write-Host "총 라인 수: $lineCount"
Write-Host "메타 저장 : $metaFile"
