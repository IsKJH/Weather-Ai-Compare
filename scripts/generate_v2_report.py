import json
import re
from datetime import datetime
from pathlib import Path

ROOT = Path(r"C:\proj\weather-ai-compare")
AIS = ["claude", "codex", "gemini"]
MODEL_NAMES = {
    "claude": "Claude Code CLI v2.1.153",
    "codex": "OpenAI Codex CLI v0.134.0 / gpt-5.5",
    "gemini": "Gemini CLI v0.43.0",
}
UI_SUMMARY = {
    "claude": {
        "theme": "밝은 블루 그라디언트, v1 감성 유지",
        "citySelector": "상단 pill 탭 + 즐겨찾기 별 표시",
        "features": ["시간별 예보", "상세 날씨 6개 카드", "새로고침/마지막 업데이트", "즐겨찾기"],
        "korean": "화면 기준 정상",
        "note": "기존 화면 스타일을 가장 많이 보존했지만 하단 시간별 예보가 내비게이션 바와 겹침",
    },
    "codex": {
        "theme": "다크 네이비 대시보드",
        "citySelector": "상단 버튼형 도시 선택",
        "features": ["시간별 예보", "상세 날씨", "새로고침", "즐겨찾기"],
        "korean": "화면 기준 정상",
        "note": "구조와 정보 위계가 가장 명확하지만 v1 대비 시각 스타일 변화가 큼",
    },
    "gemini": {
        "theme": "블루 그라디언트, 상단 액션 중심",
        "citySelector": "상단 텍스트 탭",
        "features": ["시간별 예보", "상세 날씨 6개 카드", "새로고침 아이콘", "즐겨찾기 하트"],
        "korean": "화면 기준 정상",
        "note": "앱다운 액션 배치가 좋지만 시간별 예보 영역이 하단에서 일부 잘림",
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


def command(args):
    import subprocess
    return subprocess.run(args, cwd=ROOT, capture_output=True, text=True, encoding="utf-8", errors="replace").stdout.strip()


def diff_stat(ai: str):
    return command(["git", "diff", "--stat", "weather-v1", "--", f"projects/weather-{ai}"])


def changed_files(ai: str):
    out = command(["git", "diff", "--name-status", "weather-v1", "--", f"projects/weather-{ai}"])
    return [line for line in out.splitlines() if line.strip()]


def token_info(ai: str):
    log_path = ROOT / "logs" / f"{ai}_v2.log"
    if not log_path.exists():
        return {"captured": False, "note": "로그 파일 없음"}
    text = read_full_text(log_path)
    if ai == "codex":
        m = re.search(r"tokens used\s*\r?\n\s*([0-9,]+)", text, re.IGNORECASE)
        if m:
            total = int(m.group(1).replace(",", ""))
            return {
                "captured": True,
                "total": total,
                "inputOutputSplit": "CLI 로그가 총 사용량만 제공",
                "note": "Codex CLI text output의 'tokens used' 값을 파싱",
            }
    return {
        "captured": False,
        "total": None,
        "note": "이번 v2 실행 옵션의 CLI 로그에 토큰 통계가 출력되지 않아 사후 복구 불가. v3부터 json/stream-json 출력으로 캡처 필요",
    }


def feature_check(ai: str):
    project = ROOT / "projects" / f"weather-{ai}" / "app" / "src" / "main" / "java" / "com" / "example" / "weathernow"
    text = "\n".join(p.read_text(encoding="utf-8", errors="replace") for p in project.rglob("*.kt"))
    checks = {
        "koreanFixed": all(s in text for s in ["서울", "부산", "제주"]),
        "hourlyForecast": "Hourly" in text or "시간별" in text,
        "extraDetails": all(s in text for s in ["강수", "자외선", "대기질"]),
        "refresh": "refresh" in text.lower() or "새로고침" in text,
        "favorite": "favorite" in text.lower() or "즐겨" in text,
        "fiveDayForecastPreserved": "forecast" in text and ("5일" in text or "5-Day" in text or "5-Day" in text),
    }
    checks["completedCount"] = sum(1 for v in checks.values() if v is True)
    checks["totalCount"] = 6
    return checks


def main():
    results = {}
    for ai in AIS:
        meta = read_json(ROOT / "logs" / f"{ai}_v2_meta.json")
        results[ai] = {
            "model": MODEL_NAMES[ai],
            "elapsedSec": meta.get("elapsedSec"),
            "tokens": token_info(ai),
            "code": {
                "ktFiles": meta.get("ktFileCount"),
                "ktLines": meta.get("ktLineCount"),
                "changedFileCountFromV1": meta.get("changedFileCountFromV1"),
                "changedFilesFromV1": meta.get("changedFilesFromV1"),
                "diffStatFromV1": diff_stat(ai),
            },
            "build": {
                "success": meta.get("buildSuccess"),
                "screenshotSuccess": meta.get("screenshotSuccess"),
                "screenshot": f"../screenshots/v2/{ai}.png",
                "note": "09_screenshot_v2.ps1에서 assembleDebug, APK 설치, ADB 스크린샷까지 검증",
            },
            "requirements": feature_check(ai),
            "ui": UI_SUMMARY[ai],
        }

    elapsed = {ai: results[ai]["elapsedSec"] for ai in AIS}
    changed = {ai: results[ai]["code"]["changedFileCountFromV1"] for ai in AIS}
    kt_lines = {ai: results[ai]["code"]["ktLines"] for ai in AIS}
    captured_tokens = {ai: results[ai]["tokens"].get("total") for ai in AIS if results[ai]["tokens"].get("captured")}

    report = {
        "generatedAt": datetime.now().isoformat(timespec="seconds"),
        "project": "WeatherNow Android App - AI v2 improvement comparison",
        "baselineTag": "weather-v1",
        "resultTag": "weather-v2",
        "prompt": "같은 v2 개선 프롬프트로 기존 v1 WeatherNow 앱을 수정하게 하고 결과 비교",
        "criteria": [
            "기존 프로젝트를 새로 만들지 않고 개선했는가",
            "한국어 깨짐을 복구했는가",
            "시간별 예보를 추가했는가",
            "강수확률/자외선/대기질을 추가했는가",
            "새로고침/마지막 업데이트/loading 상태를 추가했는가",
            "즐겨찾기를 ViewModel 상태로 구현했는가",
            "빌드와 실제 설치/스크린샷이 성공했는가",
            "토큰과 소요시간 대비 결과 품질이 어떤가",
        ],
        "results": results,
        "comparison": {
            "fastestGeneration": min(elapsed, key=elapsed.get),
            "slowestGeneration": max(elapsed, key=elapsed.get),
            "mostChangedFiles": max(changed, key=changed.get),
            "leastChangedFiles": min(changed, key=changed.get),
            "mostCodeAfterV2": max(kt_lines, key=kt_lines.get),
            "leastCodeAfterV2": min(kt_lines, key=kt_lines.get),
            "buildSuccess": "all",
            "screenshotSuccess": "all",
            "tokenCapture": {
                "captured": captured_tokens,
                "missing": [ai for ai in AIS if ai not in captured_tokens],
                "note": "v2 실행에서 Claude/Gemini는 토큰 통계를 로그로 남기지 않았음. 이는 모델 성능이 아니라 계측 설정 문제이므로 v3에서 json/stream-json 모드로 보완 필요",
            },
            "bestForPresentation": "Codex는 계측 가능한 토큰과 구조적 변경이 강점, Claude는 기존 UI 보존, Gemini는 앱 액션 배치가 강점",
        },
    }

    out_json = ROOT / "results" / "report_v2.json"
    out_json.write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")

    def td(x):
        if isinstance(x, bool):
            return "성공" if x else "실패"
        if x is None:
            return "로그 미제공"
        return str(x)

    rows = []
    metrics = [
        ("모델/CLI", lambda ai: results[ai]["model"]),
        ("소요 시간(초)", lambda ai: results[ai]["elapsedSec"]),
        ("토큰", lambda ai: results[ai]["tokens"].get("total") if results[ai]["tokens"].get("captured") else "로그 미제공"),
        ("토큰 비고", lambda ai: results[ai]["tokens"].get("note")),
        ("Kotlin 파일 수", lambda ai: results[ai]["code"]["ktFiles"]),
        ("Kotlin 라인 수", lambda ai: results[ai]["code"]["ktLines"]),
        ("v1 대비 변경 파일", lambda ai: results[ai]["code"]["changedFileCountFromV1"]),
        ("빌드", lambda ai: results[ai]["build"]["success"]),
        ("스크린샷", lambda ai: results[ai]["build"]["screenshotSuccess"]),
        ("요구 기능 완료", lambda ai: f"{results[ai]['requirements']['completedCount']}/{results[ai]['requirements']['totalCount']}"),
        ("한국어", lambda ai: results[ai]["ui"]["korean"]),
        ("UI 테마", lambda ai: results[ai]["ui"]["theme"]),
        ("특징", lambda ai: ", ".join(results[ai]["ui"]["features"])),
        ("관찰", lambda ai: results[ai]["ui"]["note"]),
    ]
    for label, getter in metrics:
        rows.append("<tr><th>{}</th>{}</tr>".format(label, "".join(f"<td>{td(getter(ai))}</td>" for ai in AIS)))

    req_rows = []
    req_labels = [
        ("한국어 복구", "koreanFixed"),
        ("시간별 예보", "hourlyForecast"),
        ("상세 날씨", "extraDetails"),
        ("새로고침", "refresh"),
        ("즐겨찾기", "favorite"),
        ("5일 예보 보존", "fiveDayForecastPreserved"),
    ]
    for label, key in req_labels:
        req_rows.append("<tr><th>{}</th>{}</tr>".format(label, "".join(f"<td>{td(results[ai]['requirements'][key])}</td>" for ai in AIS)))

    details = []
    for ai in AIS:
        details.append(f"""
<section>
<h2>{ai.title()}</h2>
<h3>Diff stat from weather-v1</h3>
<pre>{results[ai]['code']['diffStatFromV1']}</pre>
<h3>Changed files</h3>
<pre>{chr(10).join(results[ai]['code']['changedFilesFromV1'] or [])}</pre>
</section>
""")

    html = f"""<!doctype html>
<html lang="ko">
<head>
<meta charset="utf-8">
<title>WeatherNow AI v2 Comparison</title>
<style>
body {{ font-family: Arial, 'Malgun Gothic', sans-serif; margin: 32px; background: #f6f7f9; color: #172033; }}
h1 {{ margin-bottom: 4px; }}
.meta {{ color: #5f6b7a; margin-bottom: 24px; }}
table {{ border-collapse: collapse; width: 100%; background: #fff; margin: 18px 0 28px; }}
th, td {{ border: 1px solid #d8dde6; padding: 10px; vertical-align: top; }}
th {{ background: #eef2f7; text-align: left; width: 170px; }}
img {{ max-width: 280px; border: 1px solid #d8dde6; border-radius: 8px; }}
pre {{ white-space: pre-wrap; word-break: break-word; background: #101828; color: #e8eef7; padding: 14px; border-radius: 8px; overflow: auto; }}
section {{ background: #fff; padding: 18px; border: 1px solid #d8dde6; border-radius: 10px; margin: 18px 0; }}
.warn {{ background: #fff8e5; border: 1px solid #f0cf75; padding: 12px; border-radius: 8px; }}
</style>
</head>
<body>
<h1>WeatherNow AI v2 개선 비교</h1>
<div class="meta">Generated at {report['generatedAt']} / baseline: weather-v1 / result: weather-v2 / prompt: prompt_v2.txt</div>
<div class="warn"><b>토큰 계측 주의:</b> Codex는 로그에서 총 토큰 128,899를 파싱했습니다. Claude와 Gemini는 이번 v2 실행 옵션에서 토큰 통계를 로그로 남기지 않아 사후 복구가 불가능합니다. v3에서는 JSON/stream-json 출력으로 토큰 계측을 먼저 고정해야 합니다.</div>
<h2>종합 비교</h2>
<table><thead><tr><th>기준</th><th>Claude</th><th>Codex</th><th>Gemini</th></tr></thead><tbody>{''.join(rows)}</tbody></table>
<h2>요구사항 체크</h2>
<table><thead><tr><th>요구사항</th><th>Claude</th><th>Codex</th><th>Gemini</th></tr></thead><tbody>{''.join(req_rows)}</tbody></table>
<h2>스크린샷</h2>
<table><tr><th>Claude</th><th>Codex</th><th>Gemini</th></tr><tr><td><img src="../screenshots/v2/claude.png"></td><td><img src="../screenshots/v2/codex.png"></td><td><img src="../screenshots/v2/gemini.png"></td></tr></table>
<h2>비교 결론</h2>
<table><tbody>
<tr><th>가장 빠름</th><td colspan="3">{report['comparison']['fastestGeneration']} ({elapsed[report['comparison']['fastestGeneration']]}초)</td></tr>
<tr><th>가장 많은 변경</th><td colspan="3">{report['comparison']['mostChangedFiles']} ({changed[report['comparison']['mostChangedFiles']]}개 파일)</td></tr>
<tr><th>빌드/실행</th><td colspan="3">세 결과 모두 assembleDebug, APK 설치, 스크린샷 성공</td></tr>
<tr><th>해석</th><td colspan="3">{report['comparison']['bestForPresentation']}</td></tr>
</tbody></table>
{''.join(details)}
</body>
</html>
"""
    (ROOT / "results" / "report_v2.html").write_text(html, encoding="utf-8")
    print(out_json)
    print(ROOT / "results" / "report_v2.html")

if __name__ == "__main__":
    main()
