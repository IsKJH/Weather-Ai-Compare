import html
import json
from datetime import datetime
from pathlib import Path


ROOT = Path(r"C:\proj\weather-ai-compare")
AIS = ["claude", "codex", "gemini"]
AI_LABELS = {"claude": "Claude", "codex": "Codex", "gemini": "Gemini"}
SCREEN_LABELS = {
    "01-top": "상단 현재 날씨",
    "02-middle": "중간 기능 영역",
    "03-bottom": "하단 예보/상세",
}
PHASES = {
    "v1": {
        "json": "report.json",
        "html": "report.html",
        "title": "WeatherNow v1 초기 생성 비교",
        "eyebrow": "AI CODING TOOL COMPARISON · V1",
        "summary": "동일한 Android 날씨 앱 요구사항을 세 AI CLI에 전달해 처음부터 앱을 생성하게 한 결과입니다.",
        "prompt": "새 Android 날씨 앱 생성",
        "prompt_file": "prompt.txt",
        "baseline": "없음",
        "result": "weather-v1",
        "screens": {"claude": "../screenshots/v1/v1-claude.png", "codex": "../screenshots/v1/v1-codex.png", "gemini": "../screenshots/v1/v1-gemini.png"},
    },
    "v2": {
        "json": "report_v2.json",
        "html": "report_v2.html",
        "title": "WeatherNow v2 기능 개선 비교",
        "eyebrow": "AI CODING TOOL COMPARISON · V2",
        "summary": "v1 결과물을 기준으로 동일한 개선 프롬프트를 적용해 기능 추가, 코드 이해력, 안정성을 비교한 결과입니다.",
        "prompt": "기존 앱 기능 개선",
        "prompt_file": "prompt_v2.txt",
        "baseline": "weather-v1",
        "result": "weather-v2",
        "screens": {"claude": "../screenshots/v2/v2-claude.png", "codex": "../screenshots/v2/v2-codex.png", "gemini": "../screenshots/v2/v2-gemini.png"},
    },
    "v3": {
        "json": "report_v3.json",
        "html": "report_v3.html",
        "title": "WeatherNow v3 자율 혁신 비교",
        "eyebrow": "AI CODING TOOL COMPARISON · V3",
        "summary": "v1/v2 요구사항을 완수한 앱에 각 AI가 스스로 신선한 기능을 추가하도록 한 최종 비교 결과입니다.",
        "prompt": "자율적 혁신 기능 추가",
        "prompt_file": "prompt_v3.txt",
        "baseline": "weather-v2",
        "result": "weather-v3",
        "screens": {"claude": "../screenshots/v3/v3-claude.png", "codex": "../screenshots/v3/v3-codex.png", "gemini": "../screenshots/v3/v3-gemini.png"},
    },
}

PHASE_INSIGHTS = {
    "v1": [
        ("비교 초점", "동일한 최초 프롬프트에서 각 AI가 어떤 기본 앱 구조와 UI 방향을 선택했는지 확인하는 단계입니다."),
        ("읽는 법", "기능 완성도보다 초기 설계 성향, 한국어 처리, 화면 구성, 빌드 안정성을 중심으로 보는 것이 적절합니다."),
        ("한계", "v1은 처음부터 생성한 결과라 기존 코드 이해력이나 유지보수 능력을 평가하기에는 부족합니다."),
    ],
    "v2": [
        ("비교 초점", "v1 결과물을 새로 만들지 않고 유지한 채, 지정된 기능 요구사항을 얼마나 안정적으로 추가했는지 보는 단계입니다."),
        ("읽는 법", "시간별 예보, 상세 날씨, 새로고침, 즐겨찾기, 한국어 복구처럼 명시된 요구사항 충족 여부를 우선 비교합니다."),
        ("한계", "세 앱 모두 요구 기능을 갖췄기 때문에, 스크린샷만으로는 구현 품질이나 코드 구조 차이가 충분히 드러나지 않습니다."),
    ],
    "v3": [
        ("비교 초점", "v2 기능을 유지하면서 각 AI가 자율적으로 추가한 스마트 기능이 실제 사용자에게 보이는지 확인하는 단계입니다."),
        ("핵심 해석", "Gemini는 인사이트 카드와 온도 기반 배경 변화가 화면에서 바로 확인됩니다. Claude와 Codex는 보고된 기능명은 있으나 정적 스크린샷 기준 체감 변화가 약합니다."),
        ("주의점", "v3의 점수는 최초 정리 자료 기준이며, 최종 해석에서는 실제 diff와 캡처 화면에서 확인되는 근거를 함께 봐야 합니다."),
    ],
}

V3_EVIDENCE = {
    "claude": {
        "visible": "낮음",
        "delta": "v2와 상단/중간/하단 화면 구성이 거의 동일하게 보입니다.",
        "evidence": "현재 v2 대비 소스 diff에서 Claude 앱의 UI/로직 변경 근거가 뚜렷하게 잡히지 않습니다.",
        "interpretation": "보고서상의 Visual Logic 기능은 사용자 체감 기능으로 설명하기 어렵습니다.",
    },
    "codex": {
        "visible": "낮음",
        "delta": "v2와 정적 화면은 거의 동일합니다.",
        "evidence": "Dynamic Refresh Engine은 새로고침 후 mock 값 변동처럼 상호작용을 해야 드러나는 성격입니다.",
        "interpretation": "스크린샷만으로는 v3 차별점을 설득하기 어렵고, 별도 동작 캡처나 전후 값 비교가 필요합니다.",
    },
    "gemini": {
        "visible": "높음",
        "delta": "상단 인사이트 카드가 추가되고, 도시/날씨/온도에 따라 안내 문구와 배경 색상이 바뀝니다.",
        "evidence": "WeatherViewModel에 smartInsight 생성 로직이 추가됐고 WeatherScreen에서 인사이트 카드와 동적 배경을 표시합니다.",
        "interpretation": "v3 프롬프트의 '스마트 기능 1개' 요구를 화면에서 가장 명확하게 보여줍니다.",
    },
}

PROMPT_INTERPRETATIONS = {
    "v1": [
        ("요약", "세 AI에게 동일한 조건으로 Android 날씨 앱을 처음부터 만들게 한 프롬프트입니다."),
        ("핵심 요구", "Kotlin, Jetpack Compose, MVVM 구조를 사용하고 서울/부산/제주의 mock 날씨 데이터를 보여주는 앱을 만드는 것이 목표였습니다."),
        ("평가 관점", "초기 앱 생성 능력, 기본 UI 구성, 한국어 표시, 빌드 성공 여부를 비교하기 위한 입력입니다."),
    ],
    "v2": [
        ("요약", "v1 앱을 새로 만들지 않고 기존 코드 위에서 기능을 확장하라고 지시한 프롬프트입니다."),
        ("핵심 요구", "시간별 예보, 강수확률, 자외선 지수, 대기질, 새로고침, 마지막 업데이트, 로딩 상태, 즐겨찾기를 추가해야 했습니다."),
        ("평가 관점", "기존 코드 이해력, 요구사항 보존, 기능 추가 안정성, 작은 화면에서의 UI 완성도를 비교하기 위한 입력입니다."),
    ],
    "v3": [
        ("요약", "v1/v2 기능을 유지한 상태에서 각 AI가 스스로 스마트 기능 하나를 정해 구현하라는 자율 개선 프롬프트입니다."),
        ("핵심 요구", "정해진 기능 목록보다 창의적이고 사용자가 체감할 수 있는 AI/Smart 기능 1개를 추가하는 것이 핵심입니다."),
        ("평가 관점", "단순 리팩터링이 아니라 실제 화면이나 상호작용에서 새로운 가치가 보이는지를 비교하기 위한 입력입니다."),
    ],
}

PLAN_USAGE_NOTES = {
    "claude": {
        "plan": "Claude Pro / Claude Code",
        "basis": "공식 문서상 Claude Code 사용량은 Claude 제품군의 사용 한도와 함께 계산되며, Pro 사용자에게 고정 월간 토큰 총량이 공개되어 있지는 않습니다.",
        "interpretation": "따라서 이 실험의 토큰 수를 Pro 한도 대비 퍼센트로 환산할 수는 없고, 같은 계정의 사용량 미터나 남은 사용량 화면으로만 확인해야 합니다.",
        "source": "https://support.claude.com/en/articles/14552983-models-usage-and-limits-in-claude-code",
    },
    "codex": {
        "plan": "ChatGPT Pro / Codex",
        "basis": "Codex는 ChatGPT 플랜에 포함되지만 사용 한도는 작업 크기와 복잡도에 따라 달라지는 5시간 단위 local message/cloud task 한도와 크레딧 체계로 설명됩니다.",
        "interpretation": "토큰 사용량은 작업 규모를 비교하는 데는 유용하지만, Pro 플랜 한도 대비 정확한 퍼센트는 공개 토큰 총량이 아니라 계정별 Codex 사용량/크레딧에서 확인해야 합니다.",
        "source": "https://chatgpt.com/codex/pricing",
    },
    "gemini": {
        "plan": "Google AI Pro / Gemini CLI",
        "basis": "Gemini CLI는 무료/Pro quota와 pay-as-you-go 모델을 안내하지만, 개인 Pro 플랜에 대해 고정 월간 토큰 총량을 단일 수치로 공개하지 않습니다.",
        "interpretation": "따라서 이 실험의 토큰 수를 Pro 한도 대비 퍼센트로 산정하기보다, CLI/계정의 quota 상태와 요청 제한 도달 여부를 함께 기록하는 편이 정확합니다.",
        "source": "https://google-gemini.github.io/gemini-cli/docs/quota-and-pricing.html",
    },
}

CSS = """
*, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }
html { scroll-behavior: smooth; }
body {
  font-family: 'Segoe UI', 'Apple SD Gothic Neo', 'Malgun Gothic', '맑은 고딕', system-ui, sans-serif;
  background: #E8E8E8;
  color: #1C1C1C;
  font-size: 15px;
  line-height: 1.78;
  -webkit-font-smoothing: antialiased;
  word-break: keep-all;
}
.wrap { max-width: 1040px; margin: 0 auto; padding: 40px 24px 100px; }
.rh {
  background: #fff;
  border: 1px solid #DCDCDC;
  border-radius: 12px;
  padding: 40px 48px 32px;
  margin-bottom: 18px;
  box-shadow: 0 2px 8px rgba(0,0,0,.04);
}
.rh-eyebrow { font-size:12px; font-weight:700; letter-spacing:1.8px; text-transform:uppercase; color:#9CA3AF; margin-bottom:14px; }
.rh-title { font-size:28px; font-weight:900; line-height:1.2; margin-bottom:10px; letter-spacing:-.3px; }
.rh-sub { font-size:15.5px; color:#4B5563; line-height:1.85; margin-bottom:26px; }
.rh-meta { display:flex; gap:8px; flex-wrap:wrap; padding-top:20px; border-top:1px solid #EBEBEB; }
.ai-pill, .tag-sm {
  display:inline-flex;
  align-items:center;
  gap:7px;
  font-size:13px;
  font-weight:600;
  color:#374151;
  background:#F3F4F6;
  border:1px solid #E0E0E0;
  padding:5px 14px;
  border-radius:20px;
}
.tag-sm { color:#6B7280; background:#FAFAFA; }
.dot { display:inline-block; width:9px; height:9px; border-radius:50%; flex-shrink:0; }
.dot.claude { background:#C2622A; }
.dot.codex { background:#0D8A68; }
.dot.gemini { background:#1A56DB; }
.toc { display:flex; gap:7px; flex-wrap:wrap; margin-bottom:18px; }
.toc a {
  text-decoration:none;
  color:#6B7280;
  background:#fff;
  border:1px solid #DDD;
  border-radius:20px;
  padding:6px 13px;
  font-size:13px;
  font-weight:650;
}
.toc a:hover { color:#1C1C1C; border-color:#777; background:#F9F9F9; }
.card {
  background:#fff;
  border:1px solid #DCDCDC;
  border-radius:12px;
  padding:34px 42px;
  margin-bottom:18px;
  box-shadow:0 1px 4px rgba(0,0,0,.04);
}
.sh { display:flex; align-items:center; gap:14px; padding-bottom:18px; border-bottom:2px solid #F0F0F0; margin-bottom:24px; }
.sh-num { font-size:12px; font-weight:900; color:#A3A3A3; letter-spacing:1px; }
.sh h2 { font-size:18px; font-weight:850; color:#111; letter-spacing:-.2px; }
.lead { color:#4B5563; font-size:14.5px; margin-bottom:14px; }
.tbl-wrap { overflow-x:auto; border-radius:8px; border:1px solid #E5E5E5; margin:14px 0; }
table { width:100%; border-collapse:collapse; font-size:14px; background:#fff; }
thead th {
  font-size:11.5px;
  font-weight:800;
  text-transform:uppercase;
  letter-spacing:.5px;
  color:#9CA3AF;
  padding:11px 16px;
  text-align:left;
  border-bottom:1px solid #E8E8E8;
  background:#FAFAFA;
  white-space:nowrap;
}
tbody td { padding:13px 16px; border-bottom:1px solid #F0F0F0; vertical-align:top; }
tbody tr:last-child td { border-bottom:none; }
tbody tr:nth-child(even) td { background:#FAFAFA; }
tbody tr.row-hl td { background:#FFFBEB !important; font-weight:650; }
td.col-label { font-weight:700; color:#374151; white-space:nowrap; width:150px; }
.th-claude { border-top:3px solid #C2622A; }
.th-codex { border-top:3px solid #0D8A68; }
.th-gemini { border-top:3px solid #1A56DB; }
.metric-grid { display:grid; grid-template-columns:repeat(3,1fr); gap:14px; margin:16px 0 4px; }
.metric {
  border:1px solid #E2E2E2;
  border-radius:10px;
  padding:16px 18px;
  background:#FAFAFA;
}
.metric-k { color:#6B7280; font-size:12px; font-weight:800; text-transform:uppercase; letter-spacing:.6px; margin-bottom:4px; }
.metric-v { font-size:22px; font-weight:900; color:#111; letter-spacing:-.2px; }
.token-cards { display:flex; flex-direction:column; gap:14px; margin-bottom:10px; }
.token-card { border:1px solid #E2E2E2; border-radius:10px; overflow:hidden; }
.tc-head { display:flex; align-items:center; gap:10px; padding:11px 18px; background:#FAFAFA; border-bottom:1px solid #EBEBEB; }
.tc-name { font-weight:800; font-size:14.5px; }
.tc-model { font-size:12px; color:#9CA3AF; margin-left:auto; text-align:right; }
.kv-list { padding:4px 0; }
.kv-row { display:flex; justify-content:space-between; align-items:center; gap:20px; padding:11px 18px; border-bottom:1px solid #F5F5F5; font-size:14px; }
.kv-row:last-child { border-bottom:none; }
.kv-row.hl { background:#FFFBEB; }
.kv-k { color:#6B7280; }
.kv-v { font-family:'Consolas','Menlo',monospace; font-weight:800; font-size:13.5px; color:#111; text-align:right; }
.tc-note { padding:10px 18px; background:#FFFBEB; border-top:1px solid #FDE68A; font-size:13px; line-height:1.65; color:#78350F; }
.ss-grid { display:grid; grid-template-columns:repeat(3,1fr); gap:14px; margin:16px 0; }
.ss-card { border:1px solid #E2E2E2; border-radius:10px; overflow:hidden; background:#FAFAFA; box-shadow:0 2px 6px rgba(0,0,0,.05); }
.ss-head { display:flex; align-items:center; gap:9px; padding:10px 16px; border-bottom:1px solid #E8E8E8; font-size:13.5px; font-weight:800; color:#333; }
.ss-card img { width:100%; display:block; background:#111; }
.screen-set { display:flex; flex-direction:column; gap:22px; }
.screen-ai { border:1px solid #E2E2E2; border-radius:12px; overflow:hidden; background:#fff; }
.screen-ai-head { display:flex; align-items:center; gap:9px; padding:12px 16px; background:#FAFAFA; border-bottom:1px solid #E8E8E8; font-weight:850; }
.screen-list { display:grid; grid-template-columns:repeat(3,1fr); gap:12px; padding:14px; }
.screen-shot { border:1px solid #E5E7EB; border-radius:10px; overflow:hidden; background:#F9FAFB; }
.screen-shot-title { padding:9px 12px; border-bottom:1px solid #E5E7EB; font-size:12.5px; font-weight:800; color:#4B5563; }
.screen-shot img { width:100%; display:block; background:#111; }
.note-box { background:#FFFBEB; border-left:4px solid #F59E0B; padding:12px 16px; border-radius:0 8px 8px 0; font-size:13.5px; line-height:1.7; color:#78350F; margin:12px 0; }
.callout-grid { display:grid; grid-template-columns:repeat(3,1fr); gap:12px; margin-top:14px; }
.callout {
  border:1px solid #E5E7EB;
  border-radius:10px;
  background:#FAFAFA;
  padding:15px 16px;
}
.callout-k { font-size:12px; font-weight:850; color:#6B7280; margin-bottom:6px; }
.callout-v { font-size:13.5px; color:#374151; line-height:1.65; }
.prompt-meta { color:#6B7280; font-size:13px; font-weight:700; margin-bottom:10px; }
.prompt-pre {
  white-space:pre-wrap;
  word-break:keep-all;
  overflow:auto;
  max-height:360px;
  background:#F9FAFB;
  border:1px solid #E5E7EB;
  border-radius:8px;
  padding:14px 16px;
  font-size:13.5px;
  line-height:1.75;
  color:#374151;
}
.source-link { color:#1D4ED8; text-decoration:none; font-weight:700; }
.source-link:hover { text-decoration:underline; }
.crit-list { margin-left:18px; color:#4B5563; }
.crit-list li { margin-bottom:4px; }
pre.diff { white-space:pre-wrap; word-break:break-word; background:#F9FAFB; border:1px solid #E5E7EB; border-radius:8px; padding:12px; font-size:12px; color:#374151; overflow:auto; }
.code-card { border:1px solid #E2E2E2; border-radius:10px; overflow:hidden; margin-top:12px; }
.cc-head { display:flex; align-items:center; gap:10px; padding:11px 18px; background:#FAFAFA; border-bottom:1px solid #EBEBEB; }
.cc-name { font-weight:800; font-size:14px; }
@media (max-width: 760px) {
  .wrap { padding:24px 14px 80px; }
  .rh, .card { padding:26px 20px; }
  .rh-title { font-size:23px; }
  .metric-grid, .ss-grid, .screen-list, .callout-grid { grid-template-columns:1fr; }
  td.col-label { white-space:normal; }
  .tc-head, .kv-row { align-items:flex-start; }
  .tc-model { margin-left:0; display:block; }
}
"""


def read_json(name: str) -> dict:
    return json.loads((ROOT / "results" / name).read_text(encoding="utf-8-sig"))


def esc(value) -> str:
    return html.escape("" if value is None else str(value))


def fmt(value) -> str:
    if value is None:
        return "-"
    if isinstance(value, float):
        return f"{value:,.4f}".rstrip("0").rstrip(".")
    if isinstance(value, int):
        return f"{value:,}"
    return str(value)


def token_total(tokens: dict):
    return tokens.get("grandTotal") or tokens.get("total")


def token_rows(tokens: dict):
    rows = []
    if "grandTotal" in tokens:
        rows.append(("총 사용 토큰", fmt(tokens.get("grandTotal")), True))
    elif "total" in tokens:
        rows.append(("총 사용 토큰", fmt(tokens.get("total")), True))
    if "freshInput" in tokens:
        rows.append(("신규 입력 토큰", fmt(tokens.get("freshInput")), False))
    if "input" in tokens:
        rows.append(("입력 토큰", fmt(tokens.get("input")), False))
    if "cacheCreation" in tokens:
        rows.append(("캐시 생성 토큰", fmt(tokens.get("cacheCreation")), False))
    if "cacheRead" in tokens:
        rows.append(("캐시 재사용 토큰", fmt(tokens.get("cacheRead")), False))
    if "cachedInput" in tokens:
        rows.append(("캐시 입력 토큰", fmt(tokens.get("cachedInput")), False))
    if "output" in tokens:
        rows.append(("출력 토큰", fmt(tokens.get("output")), False))
    if "reasoningOutput" in tokens:
        rows.append(("추론 출력 토큰", fmt(tokens.get("reasoningOutput")), False))
    if "auxModelTotal" in tokens:
        rows.append(("보조 모델 토큰", fmt(tokens.get("auxModelTotal")), False))
    if "costUSD" in tokens:
        rows.append(("예상 비용", f"${fmt(tokens.get('costUSD'))}", False))
    if "mainModel" in tokens:
        rows.append(("메인 모델 토큰", fmt(tokens["mainModel"].get("total")), False))
    if "routerModel" in tokens:
        rows.append(("라우터 모델 토큰", fmt(tokens["routerModel"].get("total")), False))
    if "models" in tokens:
        for model, data in tokens["models"].items():
            rows.append((f"{model} 사용 토큰", fmt(data.get("total")), False))
    return rows


def table(headers, rows, highlights=None) -> str:
    highlights = highlights or set()
    th = "".join(f'<th class="{cls}">{esc(label)}</th>' if cls else f"<th>{esc(label)}</th>" for label, cls in headers)
    body = []
    for idx, row in enumerate(rows):
        row_class = ' class="row-hl"' if idx in highlights else ""
        cells = []
        for cidx, cell in enumerate(row):
            cls = ' class="col-label"' if cidx == 0 else ""
            cells.append(f"<td{cls}>{cell}</td>")
        body.append(f"<tr{row_class}>{''.join(cells)}</tr>")
    return f'<div class="tbl-wrap"><table><thead><tr>{th}</tr></thead><tbody>{"".join(body)}</tbody></table></div>'


def ai_table(rows, highlights=None) -> str:
    headers = [("항목", None), ("Claude", "th-claude"), ("Codex", "th-codex"), ("Gemini", "th-gemini")]
    return table(headers, rows, highlights)


def normalize_build(result: dict) -> str:
    build = result.get("build")
    if not build:
        return "미기록"
    if build.get("success") is True:
        return "성공"
    if build.get("success") is False:
        return "실패"
    return "미기록"


def normalize_screenshot(phase: str, result: dict) -> str:
    build = result.get("build") or {}
    if build.get("screenshotSuccess") is False:
        return "촬영 실패"
    return "촬영 완료"


def phase_rows(phase: str, data: dict):
    results = data["results"]
    rows = [
        ["모델", *[esc(results[ai].get("model")) for ai in AIS]],
        ["소요 시간", *[f"{fmt(results[ai].get('elapsedSec'))}초" if results[ai].get("elapsedSec") is not None else "-" for ai in AIS]],
        ["총 사용 토큰", *[fmt(token_total(results[ai].get("tokens", {}))) for ai in AIS]],
        ["Kotlin 파일/라인", *[f"{fmt(results[ai].get('code', {}).get('ktFiles'))}개 / {fmt(results[ai].get('code', {}).get('ktLines'))}줄" for ai in AIS]],
        ["빌드", *[normalize_build(results[ai]) for ai in AIS]],
        ["스크린샷", *[normalize_screenshot(phase, results[ai]) for ai in AIS]],
    ]
    if phase == "v1":
        rows.extend([
            ["UI 테마", *[esc(results[ai].get("ui", {}).get("theme")) for ai in AIS]],
            ["한국어 표시", *[esc(results[ai].get("ui", {}).get("korean")) for ai in AIS]],
        ])
    if phase == "v2":
        rows.extend([
            ["요구사항 충족", *[f"{results[ai].get('requirements', {}).get('completedCount')}/{results[ai].get('requirements', {}).get('totalCount')}" for ai in AIS]],
            ["v1 대비 변경 파일", *[f"{fmt(results[ai].get('code', {}).get('changedFileCountFromV1'))}개" for ai in AIS]],
            ["UI 테마", *[esc(results[ai].get("ui", {}).get("theme")) for ai in AIS]],
        ])
    if phase == "v3":
        rows.extend([
            ["자율 추가 기능", *[esc(results[ai].get("smartFeature")) for ai in AIS]],
            ["실제 체감 변화", *[esc(V3_EVIDENCE[ai]["visible"]) for ai in AIS]],
            ["평균 점수", *[f"{sum(results[ai].get('scores', [])) / len(results[ai].get('scores', [])):.1f}/10" if results[ai].get("scores") else "-" for ai in AIS]],
        ])
    return rows


def top_metrics(data: dict):
    results = data["results"]
    totals = {ai: token_total(results[ai].get("tokens", {})) for ai in AIS}
    elapsed = {ai: results[ai].get("elapsedSec") for ai in AIS if results[ai].get("elapsedSec") is not None}
    lines = {ai: results[ai].get("code", {}).get("ktLines") for ai in AIS if isinstance(results[ai].get("code", {}).get("ktLines"), int)}
    fastest = min(elapsed, key=elapsed.get) if elapsed else None
    lowest = min({k: v for k, v in totals.items() if v is not None}, key=lambda k: totals[k])
    most_code = max(lines, key=lines.get) if lines else None
    cards = [
        ("가장 빠른 실행", f"{AI_LABELS[fastest]} · {fmt(elapsed[fastest])}초" if fastest else "-"),
        ("최저 토큰", f"{AI_LABELS[lowest]} · {fmt(totals[lowest])}" if lowest else "-"),
        ("최대 코드량", f"{AI_LABELS[most_code]} · {fmt(lines[most_code])}줄" if most_code else "-"),
    ]
    return "".join(f'<div class="metric"><div class="metric-k">{esc(k)}</div><div class="metric-v">{esc(v)}</div></div>' for k, v in cards)


def token_cards(data: dict):
    cards = []
    for ai in AIS:
        result = data["results"][ai]
        rows = ""
        for label, value, highlight in token_rows(result.get("tokens", {})):
            cls = " kv-row hl" if highlight else " kv-row"
            rows += f'<div class="{cls.strip()}"><span class="kv-k">{esc(label)}</span><span class="kv-v">{esc(value)}</span></div>'
        note = result.get("tokens", {}).get("note")
        note_html = f'<p class="tc-note">{esc(note)}</p>' if note else ""
        cards.append(
            f'<div class="token-card"><div class="tc-head"><span class="dot {ai}"></span>'
            f'<span class="tc-name">{AI_LABELS[ai]}</span><span class="tc-model">{esc(result.get("model"))}</span></div>'
            f'<div class="kv-list">{rows}</div>{note_html}</div>'
        )
    return '<div class="token-cards">' + "".join(cards) + "</div>"


def prompt_interpretation(phase: str):
    rows = []
    for title, text in PROMPT_INTERPRETATIONS[phase]:
        rows.append([esc(title), esc(text)])
    return table([("구분", None), ("한글 해석", None)], rows)


def plan_usage_section(data: dict):
    rows = []
    for ai in AIS:
        result = data["results"][ai]
        tokens = token_total(result.get("tokens", {}))
        note = PLAN_USAGE_NOTES[ai]
        rows.append([
            f'<span class="dot {ai}"></span> {AI_LABELS[ai]}',
            fmt(tokens),
            esc(note["plan"]),
            "정확한 퍼센트 산정 불가",
            esc(note["interpretation"]),
        ])
    return table([("AI", None), ("이번 실행 토큰", None), ("비교 플랜", None), ("한도 대비", None), ("해석", None)], rows)


def plan_sources():
    rows = []
    for ai in AIS:
        note = PLAN_USAGE_NOTES[ai]
        rows.append([
            f'<span class="dot {ai}"></span> {AI_LABELS[ai]}',
            esc(note["basis"]),
            f'<a class="source-link" href="{esc(note["source"])}">{esc(note["source"])}</a>',
        ])
    return table([("AI", None), ("공식 기준 요약", None), ("출처", None)], rows)


def screenshots(phase: str):
    groups = []
    for ai in AIS:
        shots = []
        for name, label in SCREEN_LABELS.items():
            src = f"../screenshots/{phase}/{ai}/{name}.png"
            shots.append(
                f'<div class="screen-shot"><div class="screen-shot-title">{esc(label)}</div>'
                f'<img src="{src}" alt="{AI_LABELS[ai]} {label}"></div>'
            )
        groups.append(
            f'<div class="screen-ai"><div class="screen-ai-head"><span class="dot {ai}"></span>{AI_LABELS[ai]}</div>'
            f'<div class="screen-list">{"".join(shots)}</div></div>'
        )
    return '<div class="screen-set">' + "".join(groups) + "</div>"


def insight_section(phase: str):
    items = PHASE_INSIGHTS.get(phase, [])
    cards = "".join(
        f'<div class="callout"><div class="callout-k">{esc(title)}</div><div class="callout-v">{esc(text)}</div></div>'
        for title, text in items
    )
    return f'<div class="callout-grid">{cards}</div>'


def v3_evidence_section(num: int):
    rows = []
    for ai in AIS:
        item = V3_EVIDENCE[ai]
        rows.append([
            f'<span class="dot {ai}"></span> {AI_LABELS[ai]}',
            esc(item["visible"]),
            esc(item["delta"]),
            esc(item["evidence"]),
            esc(item["interpretation"]),
        ])
    headers = [("AI", None), ("체감 변화", None), ("화면상 차이", None), ("근거", None), ("해석", None)]
    return f"""
  <section class="card" id="evidence">
    <div class="sh"><span class="sh-num">{num:02d}</span><h2>v2 대비 체감 변화</h2></div>
    <p class="lead">v3는 자율 기능 추가 단계였지만, 최종 판단은 보고된 기능명보다 실제 코드 diff와 캡처 화면에서 확인되는 변화에 맞췄습니다.</p>
    {table(headers, rows, highlights={2})}
  </section>
"""


def observations(phase: str, data: dict):
    rows = []
    for ai in AIS:
        result = data["results"][ai]
        if phase in {"v1", "v2"}:
            ui = result.get("ui", {})
            features = ", ".join(ui.get("features", []))
            note = ui.get("note") or (result.get("build") or {}).get("note") or ""
            rows.append([
                f'<span class="dot {ai}"></span> {AI_LABELS[ai]}',
                esc(ui.get("theme")),
                esc(features),
                esc(note),
            ])
        else:
            scores = result.get("scores") or []
            evidence = V3_EVIDENCE[ai]
            rows.append([
                f'<span class="dot {ai}"></span> {AI_LABELS[ai]}',
                esc(result.get("smartFeature")),
                esc(evidence["interpretation"]),
                esc(f"{sum(scores) / len(scores):.1f}/10" if scores else "-"),
            ])
    if phase in {"v1", "v2"}:
        headers = [("AI", None), ("UI 테마", None), ("주요 기능", None), ("관찰", None)]
    else:
        headers = [("AI", None), ("자율 추가 기능", None), ("구현 방향", None), ("평균 점수", None)]
    return table(headers, rows)


def code_summary(phase: str, data: dict):
    rows = []
    if phase == "v3":
        for ai in AIS:
            code = data["results"][ai].get("code", {})
            recorded = len(code.get("changedFiles", []))
            rows.append([
                f'<span class="dot {ai}"></span> {AI_LABELS[ai]}',
                f"{fmt(code.get('ktFiles'))}개",
                f"{fmt(code.get('ktLines'))}줄",
                f"기록상 {recorded}개",
                esc(V3_EVIDENCE[ai]["evidence"]),
            ])
        return table([("AI", None), ("Kotlin 파일", None), ("Kotlin 라인", None), ("변경 파일", None), ("현재 확인 근거", None)], rows, highlights={2})

    for ai in AIS:
        code = data["results"][ai].get("code", {})
        changed = code.get("changedFileCountFromV1")
        changed_text = f"{fmt(changed)}개" if changed is not None else f"{len(code.get('changedFiles', []))}개"
        rows.append([
            f'<span class="dot {ai}"></span> {AI_LABELS[ai]}',
            f"{fmt(code.get('ktFiles'))}개",
            f"{fmt(code.get('ktLines'))}줄",
            changed_text if phase != "v1" else "-",
        ])
    base = table([("AI", None), ("Kotlin 파일", None), ("Kotlin 라인", None), ("변경 파일", None)], rows)
    if phase == "v2":
        details = []
        for ai in AIS:
            diff = data["results"][ai].get("code", {}).get("diffStatFromV1")
            if diff:
                details.append(f'<div class="code-card"><div class="cc-head"><span class="dot {ai}"></span><span class="cc-name">{AI_LABELS[ai]} diff</span></div><pre class="diff">{esc(diff)}</pre></div>')
        return base + "".join(details)
    return base


def criteria_section(phase: str, data: dict, num: int):
    criteria = data.get("criteria") or []
    if not criteria:
        return ""
    items = "".join(f"<li>{esc(item)}</li>" for item in criteria)
    req_rows = []
    if phase == "v3":
        title = "평가 기준"
        items += '<li>아래 점수는 최초 정리 자료의 점수이며, 실제 체감 변화는 별도 근거 표에서 보정해 해석합니다.</li>'
        for idx, label in enumerate(criteria):
            req_rows.append([label, *[f"{data['results'][ai].get('scores', [])[idx]}/10" if idx < len(data["results"][ai].get("scores", [])) else "-" for ai in AIS]])
    else:
        title = "요구사항 체크"
        req_map = [
            ("한국어 복구", "koreanFixed"),
            ("시간별 예보", "hourlyForecast"),
            ("상세 날씨", "extraDetails"),
            ("새로고침", "refresh"),
            ("즐겨찾기", "favorite"),
            ("5일 예보 보존", "fiveDayForecastPreserved"),
        ]
        for label, key in req_map:
            req_rows.append([label, *["충족" if data["results"][ai].get("requirements", {}).get(key) else "미충족" for ai in AIS]])
    return f"""
  <section class="card" id="criteria">
    <div class="sh"><span class="sh-num">{num:02d}</span><h2>{title}</h2></div>
    <ul class="crit-list">{items}</ul>
    {ai_table(req_rows)}
  </section>
"""


def render(phase: str):
    phase_info = PHASES[phase]
    data = read_json(phase_info["json"])
    generated = data.get("generatedAt") or datetime.now().isoformat(timespec="seconds")
    prompt_text = (ROOT / phase_info["prompt_file"]).read_text(encoding="utf-8-sig", errors="replace").strip()
    has_criteria = bool(data.get("criteria"))
    criteria_html = criteria_section(phase, data, 4) if has_criteria else ""
    evidence_html = v3_evidence_section(5) if phase == "v3" else ""
    section_offset = (1 if criteria_html else 0) + (1 if evidence_html else 0)
    toc_criteria = '<a href="#criteria">요구사항</a>' if criteria_html else ""
    toc_evidence = '<a href="#evidence">체감 변화</a>' if evidence_html else ""
    body = f"""<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1.0">
<title>{esc(phase_info["title"])}</title>
<link rel="stylesheet" href="report_common.css">
</head>
<body id="top">
<div class="wrap">
  <header class="rh">
    <div class="rh-eyebrow">{esc(phase_info["eyebrow"])}</div>
    <h1 class="rh-title">{esc(phase_info["title"])}</h1>
    <p class="rh-sub">{esc(phase_info["summary"])}</p>
    <div class="rh-meta">
      <span class="ai-pill"><span class="dot claude"></span>Claude</span>
      <span class="ai-pill"><span class="dot codex"></span>Codex</span>
      <span class="ai-pill"><span class="dot gemini"></span>Gemini</span>
      <span class="tag-sm">baseline: {esc(phase_info["baseline"])}</span>
      <span class="tag-sm">result: {esc(phase_info["result"])}</span>
      <span class="tag-sm">generated: {esc(generated)}</span>
    </div>
  </header>
  <nav class="toc">
    <a href="#overview">개요</a><a href="#prompt">프롬프트</a><a href="#summary">종합 비교</a>{toc_criteria}{toc_evidence}<a href="#tokens">토큰</a><a href="#screens">스크린샷</a><a href="#observations">관찰</a><a href="#code">코드</a>
  </nav>
  <section class="card" id="overview">
    <div class="sh"><span class="sh-num">01</span><h2>실험 개요</h2></div>
    <p class="lead">{esc(phase_info["summary"])}</p>
    {insight_section(phase)}
    <div class="metric-grid">{top_metrics(data)}</div>
  </section>
  <section class="card" id="prompt">
    <div class="sh"><span class="sh-num">02</span><h2>실험 프롬프트</h2></div>
    <p class="prompt-meta">{esc(phase_info["prompt_file"])} · {esc(phase_info["prompt"])}</p>
    <pre class="prompt-pre">{esc(prompt_text)}</pre>
    <div class="note-box">아래 표는 원문 프롬프트를 보고서 독자가 빠르게 이해할 수 있도록 한글로 풀어쓴 해석입니다.</div>
    {prompt_interpretation(phase)}
  </section>
  <section class="card" id="summary">
    <div class="sh"><span class="sh-num">03</span><h2>종합 비교</h2></div>
    {ai_table(phase_rows(phase, data), highlights={2, 4, 5})}
  </section>
{criteria_html}
{evidence_html}
  <section class="card" id="tokens">
    <div class="sh"><span class="sh-num">{4 + section_offset:02d}</span><h2>토큰 사용량</h2></div>
    {token_cards(data)}
    <div class="note-box">Pro 플랜은 서비스별로 고정 월간 토큰 총량이 공개된 구조가 아니므로, 아래 표는 “이번 실행 토큰 규모”와 “공식 플랜 한도 대비 해석 가능 여부”를 구분해 정리했습니다.</div>
    {plan_usage_section(data)}
    {plan_sources()}
  </section>
  <section class="card" id="screens">
    <div class="sh"><span class="sh-num">{5 + section_offset:02d}</span><h2>실행 화면</h2></div>
    <p class="lead">각 앱을 실제 기기에 설치한 뒤 상단, 중간, 하단 스크롤 위치를 동일한 방식으로 캡처했습니다.</p>
    {screenshots(phase)}
  </section>
  <section class="card" id="observations">
    <div class="sh"><span class="sh-num">{6 + section_offset:02d}</span><h2>구현 관찰</h2></div>
    {observations(phase, data)}
  </section>
  <section class="card" id="code">
    <div class="sh"><span class="sh-num">{7 + section_offset:02d}</span><h2>코드 요약</h2></div>
    {code_summary(phase, data)}
  </section>
</div>
</body>
</html>
"""
    (ROOT / "results" / phase_info["html"]).write_text(body, encoding="utf-8")
    print(ROOT / "results" / phase_info["html"])


def main():
    (ROOT / "results" / "report_common.css").write_text(CSS.strip() + "\n", encoding="utf-8")
    for phase in ["v1", "v2", "v3"]:
        render(phase)


if __name__ == "__main__":
    main()
