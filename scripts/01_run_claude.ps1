$projectDir = "C:\proj\weather-ai-compare\projects\weather-claude"
$logFile    = "C:\proj\weather-ai-compare\logs\claude.log"
$metaFile   = "C:\proj\weather-ai-compare\logs\claude_meta.json"
$prompt     = Get-Content "C:\proj\weather-ai-compare\prompt.txt" -Raw

Set-Location $projectDir

$start = Get-Date
Write-Host "[Claude] 시작: $start"

claude -p $prompt 2>&1 | Tee-Object -FilePath $logFile

$end = Get-Date
$elapsed = ($end - $start).TotalSeconds
Write-Host "[Claude] 종료: $end (소요: $elapsed 초)"

# 토큰 파싱 (Claude는 로그 마지막에 토큰 정보 출력)
$logContent = Get-Content $logFile -Raw
$inputTokens  = if ($logContent -match 'Input tokens[:\s]+([0-9,]+)')  { $matches[1] -replace ',','' } else { "N/A" }
$outputTokens = if ($logContent -match 'Output tokens[:\s]+([0-9,]+)') { $matches[1] -replace ',','' } else { "N/A" }

# 코드 라인 수 집계
$ktFiles   = Get-ChildItem $projectDir -Recurse -Filter "*.kt" -ErrorAction SilentlyContinue
$lineCount = ($ktFiles | ForEach-Object { (Get-Content $_.FullName).Count } | Measure-Object -Sum).Sum
$fileCount = $ktFiles.Count

$meta = @{
    ai            = "claude"
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
