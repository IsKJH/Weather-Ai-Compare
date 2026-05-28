param(
    [string]$AiName,
    [string]$ProjectDir,
    [string]$CommandLine
)

$root = "C:\proj\weather-ai-compare"
$logFile = Join-Path $root "logs\${AiName}_v2.log"
$metaFile = Join-Path $root "logs\${AiName}_v2_meta.json"
$prompt = Get-Content (Join-Path $root "prompt_v2.txt") -Raw

Set-Location $ProjectDir

$start = Get-Date
Write-Host "[$AiName v2] start: $start"

if ($AiName -eq "claude") {
    claude -p $prompt 2>&1 | Tee-Object -FilePath $logFile
} elseif ($AiName -eq "codex") {
    codex exec --dangerously-bypass-approvals-and-sandbox $prompt 2>&1 | Tee-Object -FilePath $logFile
} elseif ($AiName -eq "gemini") {
    gemini -p $prompt --yolo 2>&1 | Tee-Object -FilePath $logFile
} else {
    throw "Unsupported AI name: $AiName"
}

$end = Get-Date
$elapsed = ($end - $start).TotalSeconds
Write-Host "[$AiName v2] end: $end (elapsed: $elapsed sec)"

$logContent = if (Test-Path $logFile) { Get-Content $logFile -Raw } else { "" }
$inputTokens = if ($logContent -match 'prompt_tokens["\s:]+([0-9]+)') { $matches[1] } `
    elseif ($logContent -match 'input_tokens["\s:]+([0-9]+)') { $matches[1] } `
    elseif ($logContent -match 'Input[:\s]+([0-9,]+)\s*tokens') { $matches[1] -replace ',','' } `
    else { "N/A" }
$outputTokens = if ($logContent -match 'completion_tokens["\s:]+([0-9]+)') { $matches[1] } `
    elseif ($logContent -match 'output_tokens["\s:]+([0-9]+)') { $matches[1] } `
    elseif ($logContent -match 'candidates_token["\s:]+([0-9]+)') { $matches[1] } `
    elseif ($logContent -match 'Output[:\s]+([0-9,]+)\s*tokens') { $matches[1] -replace ',','' } `
    else { "N/A" }
$totalTokens = if ($logContent -match 'total_tokens["\s:]+([0-9]+)') { $matches[1] } `
    elseif ($logContent -match 'Total[:\s]+([0-9,]+)\s*tokens') { $matches[1] -replace ',','' } `
    else { "N/A" }

$ktFiles = Get-ChildItem $ProjectDir -Recurse -Filter "*.kt" -ErrorAction SilentlyContinue | Where-Object { $_.FullName -notmatch '\\build\\' }
$lineCount = ($ktFiles | ForEach-Object { (Get-Content $_.FullName).Count } | Measure-Object -Sum).Sum
$fileCount = $ktFiles.Count

$changedFiles = git -C $root diff --name-only weather-v1 -- "projects/weather-$AiName" 2>$null
$changedCount = if ($changedFiles) { @($changedFiles).Count } else { 0 }

$meta = [ordered]@{
    ai = $AiName
    phase = "v2"
    projectDir = $ProjectDir
    startTime = $start.ToString("o")
    endTime = $end.ToString("o")
    elapsedSec = [math]::Round($elapsed, 1)
    inputTokens = $inputTokens
    outputTokens = $outputTokens
    totalTokens = $totalTokens
    ktFileCount = $fileCount
    ktLineCount = $lineCount
    changedFileCountFromV1 = $changedCount
    changedFilesFromV1 = @($changedFiles)
}
$meta | ConvertTo-Json -Depth 6 | Out-File $metaFile -Encoding utf8

Write-Host "---"
Write-Host "elapsed sec: $elapsed"
Write-Host "input tokens: $inputTokens"
Write-Host "output tokens: $outputTokens"
Write-Host "total tokens: $totalTokens"
Write-Host "kt files: $fileCount"
Write-Host "kt lines: $lineCount"
Write-Host "changed files from v1: $changedCount"
Write-Host "meta saved: $metaFile"
