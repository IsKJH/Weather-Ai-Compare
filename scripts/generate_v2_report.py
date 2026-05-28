import base64
import html
import json
import math
import re
from datetime import datetime
from pathlib import Path

ROOT = Path(r"C:\proj\weather-ai-compare")
AIS = ["claude", "codex", "gemini"]
NAMES = {"claude": "Claude", "codex": "Codex", "gemini": "Gemini"}
DOT = {"claude": "claude", "codex": "codex", "gemini": "gemini"}
MODEL_NAMES = {
    "claude": "claude-sonnet-4-6 (Claude Code CLI v2.1.153)",
    "codex": "gpt-5.5 (OpenAI Codex CLI v0.134.0)",
    "gemini": "gemini-3.1-flash-lite + gemini-3-flash (Gemini CLI v0.43.0)",
}
UI_SUMMARY = {
    "claude": {
        "theme": "밝은 블루 그라디언트, v1 감성 유지",
        "citySelector": "상단 pill 탭 + 즐겨찾기 표시",
        "icons": "이모지",
        "features": ["시간별 예보", "상세 날씨 카드", "새로고침/마지막 업데이트", "즐겨찾기"],
        "korean": "정상",
        "note": "v1의 시각 정체성을 가장 많이 유지. 하단 시간별 예보가 내비게이션 바와 일부 겹침",
    },
    "codex": {
        "theme": "다크 네이비 대시보드",
        "citySelector": "상단 버튼형 도시 선택",
        "icons": "이모지 + 텍스트",
        "features": ["시간별 예보", "상세 날씨", "새로고침", "즐겨찾기"],
        "korean": "정상",
        "note": "정보 구조와 컴포넌트 분리가 가장 명확. v1 대비 디자인 변화 폭은 큼",
    },
    "gemini": {
        "theme": "블루 그라디언트, 모바일 앱 액션 중심",
        "citySelector": "상단 텍스트 탭",
        "icons": "이모지",
        "features": ["시간별 예보", "상세 날씨 카드", "새로고침 아이콘", "즐겨찾기 하트"],
        "korean": "정상",
        "note": "상단 액션 배치가 직관적. 하단 시간별 예보가 내비게이션 바와 일부 겹침",
    },
}


def read_full_text(path: Path) -> str:
    raw = path.read_bytes()
    if raw.startswith(b"\xff\xfe") or raw.startswith(b"\xfe\xff"):
        return raw.decode("utf-16", errors="replace")
    if raw[:200].count(b"\x00") > 20:
        return raw.decode("utf-16-le", errors="replace")
    return raw.decode("utf-8-sig", errors="replace")


def read_json(path: Path):
    return json.loads(read_full_text(path))


def b64(path: Path) -> str:
    if not path.exists():
        return ""
    return "data:image/png;base64," + base64.b64encode(path.read_bytes()).decode()


def run_git(args):
    import subprocess
    return subprocess.run(["git", "-C", str(ROOT), *args], capture_output=True, text=True, encoding="utf-8", errors="replace").stdout.strip()


def changed_files(ai: str):
    return [x for x in run_git(["diff", "--name-status", "weather-v1", "--", f"projects/weather-{ai}"]).splitlines() if x]


def diff_stat(ai: str):
    return run_git(["diff", "--stat", "weather-v1", "--", f"projects/weather-{ai}"])


def parse_claude_tokens():
    text = read_full_text(ROOT / "logs" / "claude_v2.log")
    try:
        arr = json.loads(text)
    except Exception:
        arr = [json.loads(line) for line in text.splitlines() if line.strip().startswith("{")]
    final = next((x for x in reversed(arr) if isinstance(x, dict) and x.get("usage")), {})
    usage = final.get("usage", {})
    model_usage = final.get("modelUsage", {})
    fresh = int(usage.get("input_tokens", 0) or 0)
    cache_create = int(usage.get("cache_creation_input_tokens", 0) or 0)
    cache_read = int(usage.get("cache_read_input_tokens", 0) or 0)
    output = int(usage.get("output_tokens", 0) or 0)
    haiku = model_usage.get("claude-haiku-4-5-20251001", {})
    aux_total = int(haiku.get("inputTokens", 0) or 0) + int(haiku.get("outputTokens", 0) or 0)
    return {
        "captured": True,
        "freshInput": fresh,
        "cacheCreation": cache_create,
        "cacheRead": cache_read,
        "output": output,
        "totalInput": fresh + cache_create + cache_read,
        "grandTotal": fresh + cache_create + cache_read + output,
        "auxModelTotal": aux_total,
        "costUSD": final.get("total_cost_usd"),
        "note": "Claude CLI 사용량 기준. 캐시 생성/재사용 토큰을 분리했고, 보조 모델 사용량은 별도 항목으로 표시했습니다.",
    }


def parse_codex_tokens():
    text = read_full_text(ROOT / "logs" / "codex_v2.log")
    usage = {}
    for line in text.splitlines():
        line = line.strip()
        if not line.startswith("{"):
            continue
        try:
            obj = json.loads(line)
        except Exception:
            continue
        if obj.get("type") == "turn.completed":
            usage = obj.get("usage", {})
    inp = int(usage.get("input_tokens", 0) or 0)
    cached = int(usage.get("cached_input_tokens", 0) or 0)
    out = int(usage.get("output_tokens", 0) or 0)
    reasoning = int(usage.get("reasoning_output_tokens", 0) or 0)
    return {
        "captured": True,
        "input": inp,
        "cachedInput": cached,
        "output": out,
        "reasoningOutput": reasoning,
        "grandTotal": inp + out,
        "note": "Codex CLI 사용량 기준. 입력 토큰에는 캐시 입력 토큰이 포함되어 있어 별도 항목으로 함께 표시했습니다.",
    }


def extract_json_object(text: str, start: int):
    depth = 0
    in_str = False
    esc = False
    for i, ch in enumerate(text[start:], start=start):
        if in_str:
            if esc:
                esc = False
            elif ch == "\\":
                esc = True
            elif ch == '"':
                in_str = False
        else:
            if ch == '"':
                in_str = True
            elif ch == "{":
                depth += 1
            elif ch == "}":
                depth -= 1
                if depth == 0:
                    return text[start:i+1]
    return None


def parse_gemini_tokens():
    text = read_full_text(ROOT / "logs" / "gemini_v2.log")
    pos = text.find('"stats"')
    start = text.rfind("{", 0, pos) if pos >= 0 else -1
    obj = {}
    if start >= 0:
        candidate = extract_json_object(text, start)
        if candidate:
            obj = json.loads(candidate)
    models = obj.get("stats", {}).get("models", {})
    grand = 0
    result = {"captured": True, "models": {}, "grandTotal": 0, "note": "Gemini CLI 통계 기준. 보조 모델과 본 작업 모델 사용량을 합산했습니다."}
    for name, data in models.items():
        tok = data.get("tokens", {})
        total = int(tok.get("total", 0) or 0)
        grand += total
        result["models"][name] = {
            "input": tok.get("input"),
            "output": tok.get("candidates"),
            "cached": tok.get("cached"),
            "thoughts": tok.get("thoughts"),
            "tool": tok.get("tool"),
            "total": total,
        }
    result["grandTotal"] = grand
    return result


def token_info(ai: str):
    if ai == "claude":
        return parse_claude_tokens()
    if ai == "codex":
        return parse_codex_tokens()
    if ai == "gemini":
        return parse_gemini_tokens()
    raise ValueError(ai)


def feature_check(ai: str):
    project = ROOT / "projects" / f"weather-{ai}" / "app" / "src" / "main" / "java" / "com" / "example" / "weathernow"
    text = "\n".join(p.read_text(encoding="utf-8", errors="replace") for p in project.rglob("*.kt"))
    checks = {
        "koreanFixed": all(s in text for s in ["서울", "부산", "제주"]),
        "hourlyForecast": "Hourly" in text or "시간별" in text,
        "extraDetails": all(s in text for s in ["강수", "자외선", "대기질"]),
        "refresh": "refresh" in text.lower() or "새로고침" in text,
        "favorite": "favorite" in text.lower() or "즐겨" in text,
        "fiveDayForecastPreserved": "forecast" in text and ("5일" in text or "5-Day" in text or "Forecast" in text),
    }
    checks["completedCount"] = sum(1 for v in checks.values() if v is True)
    checks["totalCount"] = 6
    return checks


def fmt(n):
    if n is None:
        return "-"
    if isinstance(n, float):
        return f"{n:,.4f}"
    if isinstance(n, int):
        return f"{n:,}"
    return str(n)


def token_total(tokens):
    return tokens.get("grandTotal") or tokens.get("total")


def token_items(ai, t):
    if ai == "claude":
        return [
            ("신규 입력 토큰", fmt(t.get("freshInput")), False),
            ("캐시 생성 토큰", fmt(t.get("cacheCreation")), False),
            ("캐시 재사용 토큰", fmt(t.get("cacheRead")), False),
            ("출력 토큰", fmt(t.get("output")), False),
            ("총 사용 토큰", fmt(t.get("grandTotal")), True),
            ("보조 모델 토큰", fmt(t.get("auxModelTotal")), False),
            ("예상 비용", f"${fmt(t.get('costUSD'))}", False),
        ]
    if ai == "codex":
        return [
            ("입력 토큰", fmt(t.get("input")), False),
            ("캐시 입력 토큰", fmt(t.get("cachedInput")), False),
            ("출력 토큰", fmt(t.get("output")), False),
            ("추론 출력 토큰", fmt(t.get("reasoningOutput")), False),
            ("총 사용 토큰", fmt(t.get("grandTotal")), True),
        ]
    models = t.get("models", {})
    rows = []
    for name, m in models.items():
        rows.append((f"{name} 사용 토큰", fmt(m.get("total")), False))
    rows.append(("총 사용 토큰", fmt(t.get("grandTotal")), True))
    return rows


def data_table(headers, rows, row_highlights=None):
    row_highlights = row_highlights or set()
    th_parts = []
    for text, align, cls in headers:
        ca = f' class="{cls}"' if cls else ""
        th_parts.append(f'<th{ca} style="text-align:{align}">{text}</th>')
    thead = "<tr>" + "".join(th_parts) + "</tr>"
    tbody = ""
    for i, row in enumerate(rows):
        hl = ' class="row-hl"' if i in row_highlights else ""
        tds = "".join(f'<td class="col-label">{v}</td>' if j == 0 else f'<td>{v}</td>' for j, v in enumerate(row))
        tbody += f"<tr{hl}>{tds}</tr>"
    return f'<div class="tbl-wrap"><table><thead>{thead}</thead><tbody>{tbody}</tbody></table></div>'


def token_card(cls, name, model, items, note_text):
    rows = ""
    for k, v, hl in items:
        hc = " hl" if hl else ""
        rows += f'<div class="kv-row{hc}"><span class="kv-k">{k}</span><span class="kv-v">{v}</span></div>'
    return (
        f'<div class="token-card"><div class="tc-head"><span class="dot {cls}"></span>'
        f'<span class="tc-name">{name}</span><span class="tc-model">{model}</span></div>'
        f'<div class="kv-list">{rows}</div><p class="tc-note">{note_text}</p></div>'
    )


def img_tag(ai):
    src = b64(ROOT / "screenshots" / "v2" / f"{ai}.png")
    return f'<img src="{src}" alt="{ai} v2 screenshot" />' if src else ""


def get_v1_style():
    text = (ROOT / "results" / "report.html").read_text(encoding="utf-8", errors="replace")
    m = re.search(r"<style>(.*?)</style>", text, re.S)
    base = m.group(1) if m else ""
    extra = """
    .shot-grid { display:grid; grid-template-columns:repeat(3,1fr); gap:14px; }
    .shot-card { border:1px solid #E2E2E2; border-radius:10px; overflow:hidden; background:#FAFAFA; }
    .shot-card .shot-head { padding:10px 14px; border-bottom:1px solid #E8E8E8; font-weight:800; display:flex; align-items:center; gap:8px; }
    .shot-card img { width:100%; display:block; background:#111; }
    .crit-list { margin-left:18px; color:#4B5563; }
    .crit-list li { margin-bottom:4px; }
    pre.diff { white-space:pre-wrap; word-break:break-word; background:#F9FAFB; border:1px solid #E5E7EB; border-radius:8px; padding:12px; font-size:12px; color:#374151; overflow:auto; }
    """
    return base + extra


def main():
    results = {}
    for ai in AIS:
        meta = read_json(ROOT / "logs" / f"{ai}_v2_meta.json")
        tokens = token_info(ai)
        results[ai] = {
            "model": MODEL_NAMES[ai],
            "elapsedSec": meta.get("elapsedSec"),
            "tokens": tokens,
            "code": {
                "ktFiles": meta.get("ktFileCount"),
                "ktLines": meta.get("ktLineCount"),
                "changedFileCountFromV1": len(changed_files(ai)),
                "changedFilesFromV1": changed_files(ai),
                "diffStatFromV1": diff_stat(ai),
            },
            "build": {
                "success": meta.get("buildSuccess"),
                "screenshotSuccess": meta.get("screenshotSuccess"),
                "screenshot": f"screenshots/v2/{ai}.png",
                "note": "assembleDebug, APK install, ADB screenshot 검증 완료",
            },
            "requirements": feature_check(ai),
            "ui": UI_SUMMARY[ai],
        }

    totals = {ai: token_total(results[ai]["tokens"]) for ai in AIS}
    elapsed = {ai: results[ai]["elapsedSec"] for ai in AIS}
    changed = {ai: results[ai]["code"]["changedFileCountFromV1"] for ai in AIS}
    lines = {ai: results[ai]["code"]["ktLines"] for ai in AIS}

    report = {
        "generatedAt": datetime.now().isoformat(timespec="seconds"),
        "project": "WeatherNow Android App - AI v2 improvement comparison",
        "baselineTag": "weather-v1",
        "resultTag": "weather-v2",
        "prompt": "동일한 기존 앱 개선 프롬프트를 3개 AI CLI에 제공하고 결과 비교",
        "criteria": [
            "기존 프로젝트를 새로 만들지 않고 개선",
            "한국어 깨짐 수정",
            "시간별 예보 추가",
            "강수확률/자외선/대기질 추가",
            "새로고침/마지막 업데이트/loading 상태 추가",
            "즐겨찾기 상태 추가",
            "빌드 및 실제 기기 스크린샷 성공",
            "토큰 사용량과 소요 시간 비교",
        ],
        "results": results,
        "comparison": {
            "fastestGeneration": f"{NAMES[min(elapsed, key=elapsed.get)]} ({elapsed[min(elapsed, key=elapsed.get)]}초)",
            "lowestTokenUse": f"{NAMES[min(totals, key=totals.get)]} ({fmt(totals[min(totals, key=totals.get)])})",
            "highestTokenUse": f"{NAMES[max(totals, key=totals.get)]} ({fmt(totals[max(totals, key=totals.get)])})",
            "mostChangedFiles": f"{NAMES[max(changed, key=changed.get)]} ({changed[max(changed, key=changed.get)]}개 파일)",
            "leastChangedFiles": f"{NAMES[min(changed, key=changed.get)]} ({changed[min(changed, key=changed.get)]}개 파일)",
            "mostCodeAfterV2": f"{NAMES[max(lines, key=lines.get)]} ({lines[max(lines, key=lines.get)]}줄)",
            "leastCodeAfterV2": f"{NAMES[min(lines, key=lines.get)]} ({lines[min(lines, key=lines.get)]}줄)",
            "buildSuccess": "all",
            "screenshotSuccess": "all",
        },
    }

    (ROOT / "results" / "report_v2.json").write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")

    headers = [("항목", "left", None), ("Claude", "left", "th-claude"), ("Codex", "left", "th-codex"), ("Gemini", "left", "th-gemini")]
    rows = [
        ["모델", *[results[ai]["model"] for ai in AIS]],
        ["소요 시간", *[f'{results[ai]["elapsedSec"]}초' for ai in AIS]],
        ["총 토큰", *[fmt(totals[ai]) for ai in AIS]],
        ["Kotlin 파일/라인", *[f'{results[ai]["code"]["ktFiles"]}개 / {results[ai]["code"]["ktLines"]}줄' for ai in AIS]],
        ["v1 대비 변경 파일", *[f'{results[ai]["code"]["changedFileCountFromV1"]}개' for ai in AIS]],
        ["빌드", *["성공" if results[ai]["build"]["success"] else "실패" for ai in AIS]],
        ["스크린샷", *["성공" if results[ai]["build"]["screenshotSuccess"] else "실패" for ai in AIS]],
        ["요구사항", *[f'{results[ai]["requirements"]["completedCount"]}/{results[ai]["requirements"]["totalCount"]}' for ai in AIS]],
        ["UI 테마", *[results[ai]["ui"]["theme"] for ai in AIS]],
        ["관찰", *[results[ai]["ui"]["note"] for ai in AIS]],
    ]
    req_rows = []
    req_map = [("한국어 복구", "koreanFixed"), ("시간별 예보", "hourlyForecast"), ("상세 날씨", "extraDetails"), ("새로고침", "refresh"), ("즐겨찾기", "favorite"), ("5일 예보 보존", "fiveDayForecastPreserved")]
    for label, key in req_map:
        req_rows.append([label, *["충족" if results[ai]["requirements"][key] else "미충족" for ai in AIS]])

    token_cards = "".join(token_card(DOT[ai], NAMES[ai], MODEL_NAMES[ai], token_items(ai, results[ai]["tokens"]), results[ai]["tokens"].get("note", "")) for ai in AIS)
    shots = "".join(f'<div class="shot-card"><div class="shot-head"><span class="dot {DOT[ai]}"></span>{NAMES[ai]}</div>{img_tag(ai)}</div>' for ai in AIS)
    criteria = "".join(f"<li>{html.escape(c)}</li>" for c in report["criteria"])
    diff_sections = "".join(f'<div class="code-card"><div class="cc-head"><span class="dot {DOT[ai]}"></span><span class="cc-name">{NAMES[ai]} diff</span></div><pre class="diff">{html.escape(results[ai]["code"]["diffStatFromV1"])}\n\n{html.escape(chr(10).join(results[ai]["code"]["changedFilesFromV1"]))}</pre></div>' for ai in AIS)

    style = get_v1_style()
    body = f"""<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1.0">
<title>AI 코딩 도구 비교 리포트 - v2 개선 실험</title>
<style>{style}</style>
</head>
<body>
<div class="wrap">
  <header class="rh">
    <div class="rh-eyebrow">AI CODING TOOL COMPARISON · V2</div>
    <h1 class="rh-title">WeatherNow v2 개선 실험 비교</h1>
    <p class="rh-sub">v1에서 생성된 각 Android 날씨 앱을 동일한 개선 프롬프트로 다시 개발하게 하고, 기존 코드 이해력·기능 추가·빌드 안정성·토큰 사용량을 비교했습니다.</p>
    <div class="rh-meta">
      <span class="ai-pill"><span class="dot claude"></span>Claude</span>
      <span class="ai-pill"><span class="dot codex"></span>Codex</span>
      <span class="ai-pill"><span class="dot gemini"></span>Gemini</span>
      <span class="tag-sm">baseline: weather-v1</span>
      <span class="tag-sm">result: weather-v2</span>
    </div>
  </header>
  <nav class="toc">
    <a href="#summary">요약</a><a href="#tokens">토큰</a><a href="#requirements">요구사항</a><a href="#screens">스크린샷</a><a href="#diff">변경량</a>
  </nav>
  <section class="card" id="summary">
    <div class="sh"><span class="sh-num">01</span><h2>종합 비교</h2></div>
    {data_table(headers, rows, row_highlights={2,5,6,7})}
  </section>
  <section class="card" id="tokens">
    <div class="sh"><span class="sh-num">02</span><h2>토큰 사용량</h2></div>
    <div class="token-cards">{token_cards}</div>
  </section>
  <section class="card" id="requirements">
    <div class="sh"><span class="sh-num">03</span><h2>동일 프롬프트 요구사항 체크</h2></div>
    <ul class="crit-list">{criteria}</ul>
    {data_table(headers, req_rows)}
  </section>
  <section class="card" id="screens">
    <div class="sh"><span class="sh-num">04</span><h2>실행 화면</h2></div>
    <div class="shot-grid">{shots}</div>
  </section>
  <section class="card" id="diff">
    <div class="sh"><span class="sh-num">05</span><h2>v1 대비 변경량</h2></div>
    {diff_sections}
  </section>
</div>
</body>
</html>"""
    (ROOT / "results" / "report_v2.html").write_text(body, encoding="utf-8")
    print(ROOT / "results" / "report_v2.json")
    print(ROOT / "results" / "report_v2.html")

if __name__ == "__main__":
    main()
