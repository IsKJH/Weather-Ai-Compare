$resultsDir = "C:\proj\weather-ai-compare\results"
$reportPath = "$resultsDir\report.json"
$mdPath     = "$resultsDir\report.md"
$ais        = @("claude", "codex", "gemini")

$report = @()

foreach ($ai in $ais) {
    $metaFile = "C:\proj\weather-ai-compare\logs\${ai}_meta.json"
    if (Test-Path $metaFile) {
        $meta = Get-Content $metaFile | ConvertFrom-Json
        $report += $meta
    } else {
        Write-Host "[$ai] 메타 파일 없음 — 스킵"
    }
}

# JSON 저장
$report | ConvertTo-Json -Depth 5 | Out-File $reportPath -Encoding utf8
Write-Host "JSON 리포트 저장: $reportPath"

# Markdown 리포트 생성
$md = @"
# AI Weather App 비교 리포트

생성일: $(Get-Date -Format "yyyy-MM-dd HH:mm")

## 수치 비교

| 항목 | Claude | Codex | Gemini |
|------|--------|-------|--------|
"@

$rows = @(
    @{ label = "소요 시간 (초)";   key = "elapsedSec" },
    @{ label = "입력 토큰";        key = "inputTokens" },
    @{ label = "출력 토큰";        key = "outputTokens" },
    @{ label = "KT 파일 수";       key = "ktFileCount" },
    @{ label = "총 라인 수 (KT)";  key = "ktLineCount" },
    @{ label = "빌드 성공";        key = "buildSuccess" }
)

foreach ($row in $rows) {
    $vals = $ais | ForEach-Object {
        $m = $report | Where-Object { $_.ai -eq $_ }
        if ($m) { $m.($row.key) } else { "N/A" }
    }
    $md += "`n| $($row.label) | $($vals[0]) | $($vals[1]) | $($vals[2]) |"
}

$md += @"


## 스크린샷

| Claude | Codex | Gemini |
|--------|-------|--------|
| ![claude](../screenshots/claude.png) | ![codex](../screenshots/codex.png) | ![gemini](../screenshots/gemini.png) |
"@

$md | Out-File $mdPath -Encoding utf8
Write-Host "MD 리포트 저장: $mdPath"
Write-Host "`n[완료] 리포트 생성 완료"
