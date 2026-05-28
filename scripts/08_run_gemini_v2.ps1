$root = "C:\proj\weather-ai-compare"
& (Join-Path $root "scripts\run_ai_v2_common.ps1") `
    -AiName "gemini" `
    -ProjectDir (Join-Path $root "projects\weather-gemini") `
    -CommandLine "gemini -p prompt_v2 --yolo"
