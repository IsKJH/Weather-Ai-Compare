# Weather-Ai-Compare

Claude, Codex, Gemini에게 동일한 Android 날씨 앱 과제를 단계별로 수행시킨 뒤 결과를 비교한 실험 저장소입니다.

## 보고서

GitHub Pages에서는 아래 순서로 읽으면 됩니다.

- `results/index.html`: 전체 요약 대시보드
- `results/report.html`: v1 초기 생성 비교
- `results/report_v2.html`: v2 기능 개선 비교
- `results/report_v3.html`: v3 자율 혁신 비교

저장소 루트의 `index.html`은 `results/index.html`로 이동합니다.

## 보고서 재생성

```powershell
python scripts\generate_unified_reports.py
```

생성된 HTML은 `results/` 아래에 저장되며, 스크린샷은 `screenshots/` 경로를 참조합니다.
