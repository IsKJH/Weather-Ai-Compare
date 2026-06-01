$startTime = Get-Date
Write-Host ('START: ' + $startTime.ToString('HH:mm:ss'))
Set-Location 'C:\proj\weather-ai-compare\projects\weather-gemini'
$prompt = Get-Content 'C:\proj\weather-ai-compare\prompt_v6.txt' -Raw
gemini --yolo --skip-trust --prompt $prompt
$endTime = Get-Date
Write-Host ('END: ' + $endTime.ToString('HH:mm:ss'))
Write-Host ('ELAPSED_SEC: ' + [int]($endTime - $startTime).TotalSeconds)
