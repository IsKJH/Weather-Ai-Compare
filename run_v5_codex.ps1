$startTime = Get-Date
Write-Host ('START: ' + $startTime.ToString('HH:mm:ss'))
Set-Location 'C:\proj\weather-ai-compare\projects\weather-codex'
Get-Content 'C:\proj\weather-ai-compare\prompt_v5.txt' | codex exec --dangerously-bypass-approvals-and-sandbox -s danger-full-access
$endTime = Get-Date
Write-Host ('END: ' + $endTime.ToString('HH:mm:ss'))
Write-Host ('ELAPSED_SEC: ' + [int]($endTime - $startTime).TotalSeconds)
