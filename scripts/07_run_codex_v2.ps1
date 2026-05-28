$root = "C:\proj\weather-ai-compare"
& (Join-Path $root "scripts\run_ai_v2_common.ps1") `
    -AiName "codex" `
    -ProjectDir (Join-Path $root "projects\weather-codex") `
    -CommandLine "codex exec prompt_v2"
