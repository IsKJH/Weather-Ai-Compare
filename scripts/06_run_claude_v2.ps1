$root = "C:\proj\weather-ai-compare"
& (Join-Path $root "scripts\run_ai_v2_common.ps1") `
    -AiName "claude" `
    -ProjectDir (Join-Path $root "projects\weather-claude") `
    -CommandLine "claude -p prompt_v2"
