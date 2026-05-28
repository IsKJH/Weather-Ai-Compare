import html
import json
from datetime import datetime
from pathlib import Path

ROOT = Path(r"C:\proj\weather-ai-compare")
AIS = ["claude", "codex", "gemini"]


def read_json(path: Path):
    if not path.exists():
        return None
    try:
        return json.loads(read_full_text(path))
    except Exception as exc:
        return {"error": f"failed to read {path.name}: {exc}"}


def read_full_text(path: Path):
    raw = path.read_bytes()
    if raw.startswith(b"\xff\xfe") or raw.startswith(b"\xfe\xff"):
        return raw.decode("utf-16", errors="replace")
    if raw[:200].count(b"\x00") > 20:
        return raw.decode("utf-16-le", errors="replace")
    return raw.decode("utf-8-sig", errors="replace")


def read_text(path: Path, limit=6000):
    if not path.exists():
        return ""
    text = read_full_text(path)
    return text[-limit:]


def get_diff_stats(ai: str):
    import subprocess
    project_path = f"projects/weather-{ai}"
    result = subprocess.run(
        ["git", "-C", str(ROOT), "diff", "--stat", "weather-v1", "--", project_path],
        capture_output=True,
        text=True,
        encoding="utf-8",
        errors="replace",
    )
    return result.stdout.strip()


def get_changed_files(ai: str):
    import subprocess
    project_path = f"projects/weather-{ai}"
    result = subprocess.run(
        ["git", "-C", str(ROOT), "diff", "--name-status", "weather-v1", "--", project_path],
        capture_output=True,
        text=True,
        encoding="utf-8",
        errors="replace",
    )
    return [line for line in result.stdout.splitlines() if line.strip()]


def summarize(ai: str, meta: dict):
    log_tail = read_text(ROOT / "logs" / f"{ai}_v2.log", limit=4000)
    lower = log_tail.lower()
    return {
        "mentionsBuild": "assembledebug" in lower or "build" in lower,
        "mentionsKoreanFix": "korean" in lower or "garbled" in lower or "한국" in log_tail or "깨진" in log_tail,
        "mentionsHourlyForecast": "hourly" in lower or "시간별" in log_tail,
        "mentionsFavorite": "favorite" in lower or "즐겨" in log_tail,
        "mentionsRefresh": "refresh" in lower or "업데이트" in log_tail,
        "logTail": log_tail,
    }


def main():
    results = {}
    for ai in AIS:
        meta = read_json(ROOT / "logs" / f"{ai}_v2_meta.json") or {"ai": ai, "missingMeta": True}
        results[ai] = {
            "meta": meta,
            "diffStatFromV1": get_diff_stats(ai),
            "changedFilesFromV1": get_changed_files(ai),
            "logSignals": summarize(ai, meta),
            "screenshot": f"../screenshots/v2/{ai}.png",
        }

    report = {
        "generatedAt": datetime.now().isoformat(timespec="seconds"),
        "project": "WeatherNow Android App - AI v2 improvement comparison",
        "baselineTag": "weather-v1",
        "promptFile": "prompt_v2.txt",
        "results": results,
    }

    results_dir = ROOT / "results"
    results_dir.mkdir(exist_ok=True)
    (results_dir / "report_v2.json").write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")

    def cell(value):
        if value is None:
            return "N/A"
        if isinstance(value, bool):
            return "PASS" if value else "FAIL"
        return html.escape(str(value))

    rows = []
    metrics = [
        ("Elapsed sec", lambda r: r["meta"].get("elapsedSec")),
        ("Build success", lambda r: r["meta"].get("buildSuccess")),
        ("Screenshot", lambda r: r["meta"].get("screenshotSuccess")),
        ("Kotlin files", lambda r: r["meta"].get("ktFileCount")),
        ("Kotlin lines", lambda r: r["meta"].get("ktLineCount")),
        ("Changed files from v1", lambda r: len(r.get("changedFilesFromV1", []))),
        ("Input tokens", lambda r: r["meta"].get("inputTokens")),
        ("Output tokens", lambda r: r["meta"].get("outputTokens")),
        ("Total tokens", lambda r: r["meta"].get("totalTokens")),
    ]
    for label, getter in metrics:
        rows.append("<tr><th>{}</th>{}</tr>".format(
            html.escape(label),
            "".join(f"<td>{cell(getter(results[ai]))}</td>" for ai in AIS),
        ))

    detail_sections = []
    for ai in AIS:
        r = results[ai]
        diff_stat = html.escape(r.get("diffStatFromV1") or "No diff stat")
        changed = "\n".join(r.get("changedFilesFromV1") or ["No changed files"])
        changed = html.escape(changed)
        build_tail = html.escape(str(r["meta"].get("buildOutputTail", ""))[-2500:])
        detail_sections.append(f"""
<section class="detail">
  <h2>{ai.title()}</h2>
  <h3>Diff stat from weather-v1</h3>
  <pre>{diff_stat}</pre>
  <h3>Changed files</h3>
  <pre>{changed}</pre>
  <h3>Build output tail</h3>
  <pre>{build_tail}</pre>
</section>
""")

    screenshot_cells = []
    for ai in AIS:
        screenshot = ROOT / "screenshots" / "v2" / f"{ai}.png"
        if screenshot.exists():
            screenshot_cells.append(f'<td><img src="../screenshots/v2/{ai}.png" alt="{ai} v2 screenshot"></td>')
        else:
            screenshot_cells.append(f"<td>No screenshot</td>")

    html_doc = f"""<!doctype html>
<html lang="ko">
<head>
<meta charset="utf-8">
<title>WeatherNow AI v2 Comparison</title>
<style>
body {{ font-family: Arial, sans-serif; margin: 32px; background: #f6f7f9; color: #172033; }}
h1 {{ margin-bottom: 4px; }}
.meta {{ color: #5f6b7a; margin-bottom: 24px; }}
table {{ border-collapse: collapse; width: 100%; background: #fff; margin: 18px 0 28px; }}
th, td {{ border: 1px solid #d8dde6; padding: 10px; vertical-align: top; }}
th {{ background: #eef2f7; text-align: left; }}
img {{ max-width: 280px; border: 1px solid #d8dde6; border-radius: 8px; }}
pre {{ white-space: pre-wrap; word-break: break-word; background: #101828; color: #e8eef7; padding: 14px; border-radius: 8px; overflow: auto; }}
.detail {{ background: #fff; padding: 18px; border: 1px solid #d8dde6; border-radius: 10px; margin: 18px 0; }}
</style>
</head>
<body>
<h1>WeatherNow AI v2 Improvement Comparison</h1>
<div class="meta">Generated at {html.escape(report['generatedAt'])} / baseline: weather-v1 / prompt: prompt_v2.txt</div>
<table>
<thead><tr><th>Metric</th><th>Claude</th><th>Codex</th><th>Gemini</th></tr></thead>
<tbody>{''.join(rows)}</tbody>
</table>
<h2>Screenshots</h2>
<table><tr><th>Claude</th><th>Codex</th><th>Gemini</th></tr><tr>{''.join(screenshot_cells)}</tr></table>
{''.join(detail_sections)}
</body>
</html>
"""
    (results_dir / "report_v2.html").write_text(html_doc, encoding="utf-8")
    print(f"Wrote {results_dir / 'report_v2.json'}")
    print(f"Wrote {results_dir / 'report_v2.html'}")


if __name__ == "__main__":
    main()
