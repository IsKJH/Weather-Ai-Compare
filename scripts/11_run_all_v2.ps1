$root = "C:\proj\weather-ai-compare"

Write-Host "[v2] Running Claude improvement..."
& (Join-Path $root "scripts\06_run_claude_v2.ps1")

Write-Host "[v2] Running Codex improvement..."
& (Join-Path $root "scripts\07_run_codex_v2.ps1")

Write-Host "[v2] Running Gemini improvement..."
& (Join-Path $root "scripts\08_run_gemini_v2.ps1")

Write-Host "[v2] Building apps and capturing screenshots..."
& (Join-Path $root "scripts\09_screenshot_v2.ps1")

Write-Host "[v2] Generating report..."
& (Join-Path $root "scripts\10_report_v2.ps1")

Write-Host "[v2] Done. See results\report_v2.html and results\report_v2.json"
