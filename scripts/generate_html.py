import base64, os, math

SCREENSHOTS = r"C:\proj\weather-ai-compare\screenshots"
OUTPUT      = r"C:\proj\weather-ai-compare\results\report.html"

def b64(name):
    p = os.path.join(SCREENSHOTS, name)
    if not os.path.exists(p): return ""
    with open(p, "rb") as f:
        return "data:image/png;base64," + base64.b64encode(f.read()).decode()

c_img = b64("claude.png")
d_img = b64("codex.png")
g_img = b64("gemini.png")

# ── SVG: Radar chart ─────────────────────────────────────────
def radar_svg():
    cx, cy, r_max = 250, 215, 130
    axes = [("생성 속도",-90),("토큰 효율",-30),("명세 준수",30),("자동화",90),("한국어",150),("UI 완성도",210)]
    scores = {"claude":[2,3,4,2,5,5],"codex":[3,5,3,5,1,2],"gemini":[5,4,3,3,5,4]}
    colors = {
        "claude":("#C2622A","rgba(194,98,42,0.14)"),
        "codex": ("#0D8A68","rgba(13,138,104,0.14)"),
        "gemini":("#1A56DB","rgba(26,86,219,0.14)"),
    }
    def p(r, deg):
        a = math.radians(deg)
        return cx + r*math.cos(a), cy + r*math.sin(a)
    parts = []
    for lvl in range(5,0,-1):
        r = r_max*lvl/5
        pts = " ".join(f"{p(r,a)[0]:.1f},{p(r,a)[1]:.1f}" for _,a in axes)
        fill = "#F5F5F5" if lvl%2 else "#EEEEEE"
        parts.append(f'<polygon points="{pts}" fill="{fill}" stroke="#DCDCDC" stroke-width="0.8"/>')
    for _,ang in axes:
        x2,y2 = p(r_max,ang)
        parts.append(f'<line x1="{cx}" y1="{cy}" x2="{x2:.1f}" y2="{y2:.1f}" stroke="#CFCFCF" stroke-width="1"/>')
    for ai in ["codex","gemini","claude"]:
        stroke,fill = colors[ai]
        pts = " ".join(f"{p(r_max*s/5,a)[0]:.1f},{p(r_max*s/5,a)[1]:.1f}" for s,(_,a) in zip(scores[ai],axes))
        parts.append(f'<polygon points="{pts}" fill="{fill}" stroke="{stroke}" stroke-width="2.5" stroke-linejoin="round"/>')
        for s,(_,a) in zip(scores[ai],axes):
            x,y = p(r_max*s/5,a)
            parts.append(f'<circle cx="{x:.1f}" cy="{y:.1f}" r="3.8" fill="{stroke}" stroke="white" stroke-width="1.5"/>')
    for lbl,ang in axes:
        x,y = p(r_max+26,ang)
        parts.append(f'<text x="{x:.1f}" y="{y:.1f}" text-anchor="middle" dominant-baseline="middle" font-size="11.5" font-weight="600" fill="#555">{lbl}</text>')
    for lvl in range(1,6):
        r = r_max*lvl/5
        x,y = p(r,-90)
        parts.append(f'<text x="{x+6:.1f}" y="{y:.1f}" font-size="9" fill="#C8C8C8" dominant-baseline="middle">{lvl}</text>')
    ly = 408
    for i,(nm,col) in enumerate([("Claude","#C2622A"),("Codex","#0D8A68"),("Gemini","#1A56DB")]):
        lx = 120+i*110
        parts.append(f'<rect x="{lx}" y="{ly-5}" width="10" height="10" rx="2" fill="{col}"/>')
        parts.append(f'<text x="{lx+14}" y="{ly+0.5}" font-size="12" fill="#555" dominant-baseline="middle" font-weight="600">{nm}</text>')
    body = "\n".join(parts)
    return f'<svg viewBox="0 0 500 430" style="width:100%;max-width:480px;display:block;margin:0 auto" font-family="Segoe UI,Malgun Gothic,sans-serif">{body}</svg>'

# ── SVG: Donut chart ─────────────────────────────────────────
def make_donut(name, cls, total, segments, note):
    cx = cy = 90
    r, sw = 65, 20
    C = 2*math.pi*r
    parts = []
    parts.append(f'<circle cx="{cx}" cy="{cy}" r="{r}" fill="none" stroke="#F0F0F0" stroke-width="{sw}"/>')
    cum = 0.0
    for lbl, frac, color in segments:
        if frac < 0.002: continue
        dash = frac*C
        parts.append(
            f'<circle cx="{cx}" cy="{cy}" r="{r}" fill="none" '
            f'stroke="{color}" stroke-width="{sw}" '
            f'stroke-dasharray="{dash:.2f} {C-dash:.2f}" '
            f'stroke-dashoffset="{-cum:.2f}" '
            f'transform="rotate(-90 {cx} {cy})"/>'
        )
        cum += dash
    total_fmt = f"{total:,}"
    parts.append(f'<text x="{cx}" y="{cy-7}" text-anchor="middle" font-size="11" font-weight="800" fill="#1A1A1A">{total_fmt}</text>')
    parts.append(f'<text x="{cx}" y="{cy+8}" text-anchor="middle" font-size="9" fill="#AAA">Raw 합계</text>')
    svg_html = '<svg viewBox="0 0 180 180" style="width:155px;height:155px;flex-shrink:0" font-family="Segoe UI,sans-serif">' + "\n".join(parts) + "</svg>"
    legend_html = ""
    for lbl, frac, color in segments:
        pct = frac*100
        legend_html += (
            f'<div style="display:flex;align-items:center;gap:8px;margin-bottom:6px">'
            f'<span style="width:9px;height:9px;border-radius:2px;background:{color};flex-shrink:0"></span>'
            f'<span style="font-size:12.5px;color:#444">{lbl}</span>'
            f'<span style="font-size:11.5px;font-family:Consolas;color:#999;margin-left:auto">{pct:.1f}%</span>'
            f'</div>'
        )
    dot = f'<span class="dot {cls}"></span>'
    return (
        '<div style="border:1px solid #E2E2E2;border-radius:8px;overflow:hidden">'
        f'<div style="display:flex;align-items:center;gap:9px;padding:9px 16px;background:#FAFAFA;border-bottom:1px solid #E8E8E8">'
        f'{dot}<span style="font-weight:700;font-size:13.5px">{name}</span></div>'
        '<div style="display:flex;gap:18px;align-items:center;padding:16px 18px">'
        f'{svg_html}<div style="flex:1;min-width:0">{legend_html}'
        f'<div style="margin-top:8px;padding-top:8px;border-top:1px solid #F0F0F0;font-size:12px;color:#888">{note}</div>'
        '</div></div></div>'
    )

# ── HTML helpers ─────────────────────────────────────────────
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
        tds = "".join(
            f'<td class="col-label">{v}</td>' if j==0 else f'<td>{v}</td>'
            for j,v in enumerate(row)
        )
        tbody += f"<tr{hl}>{tds}</tr>"
    return f'<div class="tbl-wrap"><table><thead>{thead}</thead><tbody>{tbody}</tbody></table></div>'

def token_card(cls, name, model, items, note_text):
    rows = ""
    for k,v,hl in items:
        hc = " hl" if hl else ""
        rows += f'<div class="kv-row{hc}"><span class="kv-k">{k}</span><span class="kv-v">{v}</span></div>'
    return (
        f'<div class="token-card"><div class="tc-head"><span class="dot {cls}"></span>'
        f'<span class="tc-name">{name}</span><span class="tc-model">{model}</span></div>'
        f'<div class="kv-list">{rows}</div><p class="tc-note">{note_text}</p></div>'
    )

def pc_block(cls, name, pros, cons):
    pl = "".join(f"<li>{p}</li>" for p in pros)
    cl = "".join(f"<li>{c}</li>" for c in cons)
    return (
        f'<div class="pc-card"><div class="pc-head"><span class="dot {cls}"></span>'
        f'<span class="pc-name">{name}</span></div>'
        f'<div class="pc-cols">'
        f'<div class="pc-col pros"><div class="pc-col-label">장점</div><ul>{pl}</ul></div>'
        f'<div class="pc-col cons"><div class="pc-col-label">단점</div><ul>{cl}</ul></div>'
        f'</div></div>'
    )

def code_block(cls, name, rows):
    trs = "".join(f'<tr><td class="col-label">{k}</td><td>{v}</td></tr>' for k,v in rows)
    return (
        f'<div class="code-card"><div class="cc-head"><span class="dot {cls}"></span>'
        f'<span class="cc-name">{name}</span></div>'
        f'<div class="tbl-wrap"><table><tbody>{trs}</tbody></table></div></div>'
    )

def hbar(ai, cls, tok, pct):
    return (
        f'<div class="hbar-row"><span class="hbar-label">{ai}</span>'
        f'<div class="hbar-track"><div class="hbar-fill {cls}" style="width:{pct}%">'
        f'<span class="hbar-text">{tok}</span></div></div></div>'
    )

def score_bar(val, max_val=5):
    pct = val / max_val * 100
    color = {5:"#15803D", 4:"#16A34A", 3:"#B45309", 2:"#DC4E10", 1:"#B91C1C"}.get(val, "#555")
    dots = "".join(
        f'<span style="width:12px;height:12px;border-radius:50%;background:{color if i < val else "#E5E5E5"};display:inline-block"></span>'
        for i in range(max_val)
    )
    return (
        f'<div style="display:flex;align-items:center;gap:8px">'
        f'<div style="display:flex;gap:3px">{dots}</div>'
        f'<span style="font-size:13px;font-weight:700;color:{color}">{val}</span>'
        f'</div>'
    )

def callout(text):
    return f'<div class="callout">{text}</div>'

def note_box(text):
    return f'<div class="note-box">{text}</div>'

def insight(icon, text):
    return (
        f'<div class="insight"><span class="i-icon">{icon}</span>'
        f'<span class="i-text">{text}</span></div>'
    )

def rec(cls, situation, name, reason):
    return (
        f'<div class="rec {cls}"><p class="rec-sit">{situation}</p>'
        f'<p class="rec-name"><span class="dot {cls}"></span>{name}</p>'
        f'<p class="rec-why">{reason}</p></div>'
    )

def ss_card(cls, name, img_src):
    img = f'<img src="{img_src}" alt="{name}">' if img_src else '<div class="ss-placeholder">스크린샷 없음</div>'
    return (
        f'<div class="ss-card"><div class="ss-head"><span class="dot {cls}"></span>{name}</div>'
        f'{img}</div>'
    )

def snippet(title, code):
    safe = code.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;")
    return (
        f'<div style="margin:12px 0">'
        f'<p style="font-size:11px;font-weight:700;color:#888;text-transform:uppercase;letter-spacing:.5px;margin-bottom:6px">{title}</p>'
        f'<pre style="background:#1C1C2E;color:#CBD5E1;padding:14px 18px;border-radius:6px;font-size:12.5px;'
        f'font-family:Consolas,monospace;overflow-x:auto;line-height:1.7;margin:0">{safe}</pre></div>'
    )

def bench_bar(label, pct, value):
    return (
        f'<div class="bench-bar">'
        f'<span class="bench-bar-label">{label}</span>'
        f'<div class="bench-bar-track"><div class="bench-bar-fill" style="width:{pct}%"></div></div>'
        f'<span class="bench-bar-val">{value}</span>'
        f'</div>'
    )

def sb(grade):
    symbols = {"h": "◎", "m": "○", "l": "△"}
    return f'<span class="sb {grade}">{symbols[grade]}</span>'

def rt(level, text):
    return f'<span class="rt {level}">{text}</span>'

def svc_pc_block(name, tag, pros, cons):
    pl = "".join(f"<li>{p}</li>" for p in pros)
    cl = "".join(f"<li>{c}</li>" for c in cons)
    return (
        f'<div class="pc-card"><div class="pc-head">'
        f'<span class="pc-name">{name}</span>'
        f'<span class="svc-tag">{tag}</span></div>'
        f'<div class="pc-cols">'
        f'<div class="pc-col pros"><div class="pc-col-label">장점</div><ul>{pl}</ul></div>'
        f'<div class="pc-col cons"><div class="pc-col-label">단점</div><ul>{cl}</ul></div>'
        f'</div></div>'
    )

def check_item(title, desc):
    return (
        f'<div class="check-item"><div class="check-icon">✓</div>'
        f'<div><div class="check-title">{title}</div>'
        f'<div class="check-desc">{desc}</div></div></div>'
    )

def src_item(num, title, desc, url=None):
    link = f'<br><a href="{url}" target="_blank" rel="noreferrer">{url}</a>' if url else ""
    return (
        f'<div class="src-item"><span class="src-num">{num}</span>'
        f'<div class="src-body"><strong>{title}</strong> — {desc}{link}</div></div>'
    )

def matrix_row(task, *grades):
    cells = "".join(f'<td style="text-align:center">{sb(g)}</td>' for g in grades)
    return f'<tr><td class="col-label">{task}</td>{cells}</tr>'

# ── CSS ──────────────────────────────────────────────────────
css = """
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
.wrap { max-width: 960px; margin: 0 auto; padding: 40px 24px 100px; }

/* ── Report header ── */
.rh {
  background: #fff;
  border: 1px solid #DCDCDC;
  border-radius: 12px;
  padding: 40px 48px 32px;
  margin-bottom: 18px;
  box-shadow: 0 2px 8px rgba(0,0,0,.04);
}
.rh-eyebrow { font-size:12px; font-weight:700; letter-spacing:1.8px; text-transform:uppercase; color:#9CA3AF; margin-bottom:14px; }
.rh-title   { font-size:28px; font-weight:900; line-height:1.2; margin-bottom:10px; letter-spacing:-.3px; }
.rh-sub     { font-size:15.5px; color:#4B5563; line-height:1.85; margin-bottom:26px; }
.rh-meta    { display:flex; gap:8px; flex-wrap:wrap; padding-top:20px; border-top:1px solid #EBEBEB; }
.ai-pill    { display:inline-flex; align-items:center; gap:7px; font-size:13px; font-weight:600; color:#374151; background:#F3F4F6; border:1px solid #E0E0E0; padding:5px 14px; border-radius:20px; }
.tag-sm     { font-size:13px; color:#9CA3AF; padding:5px 14px; border-radius:20px; border:1px solid #E0E0E0; background:#F9FAFB; }

/* ── Dot ── */
.dot { display:inline-block; width:9px; height:9px; border-radius:50%; flex-shrink:0; }
.dot.claude { background:#C2622A; }
.dot.codex  { background:#0D8A68; }
.dot.gemini { background:#1A56DB; }

/* ── TOC ── */
.toc { display:flex; gap:7px; flex-wrap:wrap; margin-bottom:18px; }
.toc a {
  font-size:13px; font-weight:500; color:#6B7280;
  text-decoration:none; padding:6px 15px;
  border:1px solid #D5D5D5; border-radius:20px; background:#fff;
  transition:all .15s;
}
.toc a:hover { color:#1C1C1C; border-color:#777; background:#F9F9F9; }

/* ── Card ── */
.card {
  background: #fff;
  border: 1px solid #DCDCDC;
  border-radius: 12px;
  padding: 36px 44px;
  margin-bottom: 18px;
  box-shadow: 0 1px 4px rgba(0,0,0,.04);
}

/* ── Section header ── */
.sh {
  display: flex;
  align-items: center;
  gap: 14px;
  padding-bottom: 18px;
  border-bottom: 2px solid #F0F0F0;
  margin-bottom: 28px;
}
.sh-num {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px; height: 28px;
  border-radius: 6px;
  background: #F3F4F6;
  font-size: 12px; font-weight: 800; color: #9CA3AF;
  flex-shrink: 0;
}
.sh-title { font-size: 18px; font-weight: 800; color: #111; letter-spacing: -.2px; }

/* ── Sub-section label ── */
.div-label {
  font-size: 12px; font-weight: 700; text-transform: uppercase;
  letter-spacing: .8px; color: #9CA3AF;
  margin: 26px 0 12px;
  display: flex; align-items: center; gap: 8px;
}
.div-label::after {
  content: ""; flex: 1; height: 1px; background: #EBEBEB;
}

/* ── Table ── */
.tbl-wrap { overflow-x: auto; border-radius: 8px; border: 1px solid #E5E5E5; margin: 14px 0; }
table { width: 100%; border-collapse: collapse; font-size: 14px; }
thead th {
  font-size: 11.5px; font-weight: 700; text-transform: uppercase;
  letter-spacing: .5px; color: #9CA3AF;
  padding: 11px 16px; text-align: left;
  border-bottom: 1px solid #E8E8E8;
  background: #FAFAFA;
  white-space: nowrap;
}
thead th.c-claude { color: #C2622A; }
thead th.c-codex  { color: #0D8A68; }
thead th.c-gemini { color: #1A56DB; }
tbody td { padding: 13px 16px; border-bottom: 1px solid #F0F0F0; vertical-align: middle; }
tbody tr:last-child td { border-bottom: none; }
tbody tr:nth-child(even) td { background: #FAFAFA; }
tbody tr:hover td { background: #F4F4F5 !important; }
tbody tr.row-hl td { background: #FFFBEB !important; font-weight: 600; }
td.col-label { font-weight: 600; color: #374151; white-space: nowrap; }
.ok  { color: #15803D; font-weight: 700; }
.no  { color: #B91C1C; font-weight: 700; }
.num { font-family: 'Consolas', 'Menlo', monospace; }
.c-gray { color: #ABABAB; font-size: 13.5px; }

/* ── Two-column layout ── */
.two-col { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; margin: 16px 0; }

/* ── Callout / Note ── */
.callout {
  background: #EFF6FF; border-left: 4px solid #3B82F6;
  padding: 14px 18px; border-radius: 0 8px 8px 0;
  font-size: 14px; line-height: 1.7; color: #1E40AF; margin: 16px 0;
}
.note-box {
  background: #FFFBEB; border-left: 4px solid #F59E0B;
  padding: 12px 16px; border-radius: 0 8px 8px 0;
  font-size: 13.5px; line-height: 1.7; color: #78350F; margin: 12px 0;
}

/* ── Token cards ── */
.token-cards { display: flex; flex-direction: column; gap: 14px; margin-bottom: 26px; }
.token-card  { border: 1px solid #E2E2E2; border-radius: 10px; overflow: hidden; }
.tc-head  { display: flex; align-items: center; gap: 10px; padding: 11px 18px; background: #FAFAFA; border-bottom: 1px solid #EBEBEB; }
.tc-name  { font-weight: 700; font-size: 14.5px; }
.tc-model { font-size: 12px; color: #B0B0B0; margin-left: auto; }
.kv-list  { padding: 4px 0; }
.kv-row   { display: flex; justify-content: space-between; align-items: center; padding: 11px 18px; border-bottom: 1px solid #F5F5F5; font-size: 14px; }
.kv-row:last-child { border-bottom: none; }
.kv-row.hl { background: #FFFBEB; }
.kv-k { color: #6B7280; }
.kv-v { font-family: 'Consolas', monospace; font-weight: 700; font-size: 13.5px; }
.tc-note { padding: 10px 18px; background: #FFFBEB; border-top: 1px solid #FDE68A; font-size: 13px; line-height: 1.65; color: #78350F; }

/* ── Pros/Cons ── */
.pc-cards { display: flex; flex-direction: column; gap: 14px; }
.pc-card  { border: 1px solid #E2E2E2; border-radius: 10px; overflow: hidden; }
.pc-head  { display: flex; align-items: center; gap: 10px; padding: 11px 18px; background: #FAFAFA; border-bottom: 1px solid #EBEBEB; }
.pc-name  { font-weight: 700; font-size: 14.5px; }
.pc-cols  { display: grid; grid-template-columns: 1fr 1fr; }
.pc-col   { padding: 18px 22px; }
.pc-col.pros { border-right: 1px solid #EBEBEB; }
.pc-col-label { font-size: 12px; font-weight: 800; text-transform: uppercase; letter-spacing: .7px; margin-bottom: 12px; }
.pc-col.pros .pc-col-label { color: #15803D; }
.pc-col.cons .pc-col-label { color: #B91C1C; }
.pc-col ul { padding-left: 18px; }
.pc-col li { font-size: 14px; padding: 4px 0; color: #374151; line-height: 1.6; }

/* ── Code analysis cards ── */
.code-cards { display: flex; flex-direction: column; gap: 14px; }
.code-card  { border: 1px solid #E2E2E2; border-radius: 10px; overflow: hidden; }
.cc-head    { display: flex; align-items: center; gap: 10px; padding: 11px 18px; background: #FAFAFA; border-bottom: 1px solid #EBEBEB; }
.cc-name    { font-weight: 700; font-size: 14.5px; }
.code-card tbody td { padding: 11px 18px; font-size: 13.5px; }
.code-card td.col-label { width: 90px; color: #6B7280; font-weight: 700; font-size: 13px; }

/* ── Screenshot grid ── */
.ss-grid { display: grid; grid-template-columns: repeat(3,1fr); gap: 14px; margin: 16px 0; }
.ss-card { border: 1px solid #E2E2E2; border-radius: 10px; overflow: hidden; box-shadow: 0 2px 6px rgba(0,0,0,.05); }
.ss-head { display: flex; align-items: center; gap: 9px; padding: 10px 16px; background: #FAFAFA; border-bottom: 1px solid #E8E8E8; font-size: 13.5px; font-weight: 700; color: #333; }
.ss-card img { width: 100%; display: block; }
.ss-placeholder { aspect-ratio: 9/16; background: #F5F5F5; display: flex; align-items: center; justify-content: center; font-size: 13px; color: #C0C0C0; }

/* ── Bar chart ── */
.hbar-row   { display: flex; align-items: center; gap: 14px; margin-bottom: 11px; }
.hbar-label { width: 72px; font-size: 13px; font-weight: 700; text-align: right; color: #555; }
.hbar-track { flex: 1; background: #E8E8E8; border-radius: 6px; height: 30px; overflow: hidden; }
.hbar-fill  { height: 100%; border-radius: 6px; display: flex; align-items: center; padding-left: 12px; min-width: 56px; }
.hbar-fill.claude { background: linear-gradient(90deg, #C2622A, #D4773A); }
.hbar-fill.codex  { background: linear-gradient(90deg, #0D8A68, #15A07A); }
.hbar-fill.gemini { background: linear-gradient(90deg, #1A56DB, #2563EB); }
.hbar-text  { font-size: 12.5px; font-weight: 700; color: white; white-space: nowrap; text-shadow: 0 1px 2px rgba(0,0,0,.15); }

/* ── Recommendation cards ── */
.rec-grid { display: grid; grid-template-columns: repeat(3,1fr); gap: 14px; margin: 16px 0; }
.rec { border: 1px solid #E2E2E2; border-radius: 10px; padding: 20px 20px 18px; }
.rec.claude { border-top: 4px solid #C2622A; }
.rec.codex  { border-top: 4px solid #0D8A68; }
.rec.gemini { border-top: 4px solid #1A56DB; }
.rec-sit  { font-size: 11px; font-weight: 700; text-transform: uppercase; letter-spacing: 1px; color: #9CA3AF; margin-bottom: 12px; }
.rec-name { display: flex; align-items: center; gap: 9px; font-size: 16px; font-weight: 900; margin-bottom: 9px; }
.rec-why  { font-size: 13.5px; color: #555; line-height: 1.7; }

/* ── Insight rows ── */
.insight {
  display: flex; gap: 14px; align-items: flex-start;
  padding: 13px 16px; border-radius: 8px;
  background: #FAFAFA; border: 1px solid #E8E8E8;
  margin: 9px 0;
}
.i-icon { font-size: 1.05rem; line-height: 1.78; flex-shrink: 0; }
.i-text { font-size: 14px; color: #374151; line-height: 1.7; }

/* ── Tag row ── */
.tag-row { display: flex; flex-wrap: wrap; gap: 8px; margin: 12px 0; }
.tag { font-size: 13px; font-weight: 500; color: #4B5563; background: #F3F4F6; border: 1px solid #E0E0E0; padding: 5px 13px; border-radius: 5px; }

/* ── Score table ── */
.score-table { width: 100%; border-collapse: collapse; font-size: 14px; margin: 14px 0; }
.score-table th { font-size: 12px; font-weight: 700; text-transform: uppercase; letter-spacing: .5px; color: #9CA3AF; padding: 10px 14px; border-bottom: 1px solid #E8E8E8; text-align: left; background: #FAFAFA; }
.score-table td { padding: 13px 14px; border-bottom: 1px solid #F0F0F0; vertical-align: middle; }
.score-table tr:nth-child(even) td { background: #FAFAFA; }
.score-table tr:hover td { background: #F4F4F5 !important; }
.score-table tr:last-child td { border-bottom: none; font-weight: 700; background: #FFFBEB !important; }

/* ── Intro paragraph ── */
.intro { font-size: 15px; color: #4B5563; line-height: 1.85; margin-bottom: 18px; }

/* ── Back to top ── */
#back-top {
  position: fixed; bottom: 32px; right: 28px;
  width: 40px; height: 40px;
  background: #1C1C1C; color: white;
  border-radius: 50%; border: none; cursor: pointer;
  font-size: 18px; line-height: 1;
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 2px 8px rgba(0,0,0,.2);
  opacity: 0; transition: opacity .2s;
  text-decoration: none;
}
#back-top.show { opacity: 1; }

footer { text-align: center; font-size: 13px; color: #C0C0C0; padding: 32px 0 0; }

@media (max-width: 680px) {
  .ss-grid, .rec-grid, .pc-cols, .two-col { grid-template-columns: 1fr; }
  .card, .rh { padding: 22px 20px; }
  .sh-title { font-size: 16px; }
  .rh-title { font-size: 22px; }
  .sub-metrics, .check-grid { grid-template-columns: 1fr; }
}

/* ── Subscription comparison ── */
.sub-metrics { display: grid; grid-template-columns: repeat(3,1fr); gap: 14px; margin: 14px 0; }
.sub-metric { border: 1px solid #E2E2E2; border-radius: 10px; padding: 18px 20px; }
.sub-metric-label { font-size: 12px; font-weight: 700; text-transform: uppercase; letter-spacing: .5px; color: #9CA3AF; margin-bottom: 8px; }
.sub-metric-value { font-size: 16px; font-weight: 800; line-height: 1.35; color: #111; margin-bottom: 6px; }
.sub-metric-desc { font-size: 13px; color: #6B7280; line-height: 1.6; }

/* ── Score badges ◎○△ ── */
.sb { display: inline-flex; align-items: center; justify-content: center; width: 24px; height: 24px; border-radius: 7px; font-size: 12px; font-weight: 900; }
.sb.h { background: #DCFCE7; color: #15803D; }
.sb.m { background: #EFF6FF; color: #1D4ED8; }
.sb.l { background: #FFF7ED; color: #C2410C; }

/* ── Risk tags ── */
.rt { display: inline-flex; padding: 3px 9px; border-radius: 999px; font-size: 12px; font-weight: 700; }
.rt.low { background: #ECFDF5; color: #15803D; }
.rt.med { background: #FFF7ED; color: #C2410C; }
.rt.hi  { background: #FEF2F2; color: #B91C1C; }

/* ── Service tag ── */
.svc-tag { font-size: 11px; font-weight: 700; padding: 3px 10px; border-radius: 999px; background: #F3F4F6; color: #6B7280; margin-left: auto; white-space: nowrap; }

/* ── Benchmark bars ── */
.bench-bar { display: flex; align-items: center; gap: 12px; margin-bottom: 10px; }
.bench-bar-label { width: 150px; font-size: 13px; font-weight: 700; color: #374151; flex-shrink: 0; }
.bench-bar-track { flex: 1; height: 10px; background: #E8E8E8; border-radius: 5px; overflow: hidden; }
.bench-bar-fill  { height: 100%; border-radius: 5px; background: linear-gradient(90deg, #2563EB, #7C3AED); }
.bench-bar-val   { width: 46px; text-align: right; font-size: 12.5px; font-weight: 700; color: #6B7280; }

/* ── Checklist grid ── */
.check-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; margin: 14px 0; }
.check-item { display: flex; gap: 10px; align-items: flex-start; padding: 13px 15px; border: 1px solid #E5E5E5; border-radius: 9px; }
.check-icon { flex-shrink: 0; width: 21px; height: 21px; background: #EFF6FF; color: #2563EB; border-radius: 6px; display: flex; align-items: center; justify-content: center; font-size: 12px; font-weight: 900; }
.check-title { font-weight: 700; font-size: 14px; line-height: 1.35; }
.check-desc  { font-size: 12.5px; color: #6B7280; margin-top: 3px; line-height: 1.5; }

/* ── Source list ── */
.src-list { display: flex; flex-direction: column; gap: 10px; margin: 14px 0; }
.src-item { display: flex; gap: 12px; align-items: flex-start; padding: 14px 16px; border: 1px solid #E5E5E5; border-radius: 9px; }
.src-num  { flex-shrink: 0; width: 25px; height: 25px; background: #EEF2FF; color: #3730A3; border-radius: 7px; display: flex; align-items: center; justify-content: center; font-size: 12px; font-weight: 900; }
.src-body { font-size: 13.5px; line-height: 1.7; color: #374151; }
.src-body a { color: #2563EB; font-weight: 600; text-decoration: underline; text-underline-offset: 2px; }
"""

# ── Pre-compute charts ────────────────────────────────────────
radar = radar_svg()

claude_donut = make_donut(
    "Claude", "claude", 918278,
    [
        ("캐시 읽기 (865,307)", 865307/918278, "#6366F1"),
        ("출력 (40,626)",       40626/918278,  "#C2622A"),
        ("캐시 생성 (12,305)",  12305/918278,  "#F59E0B"),
        ("신규 입력 (40)",      40/918278,     "#D1D5DB"),
    ],
    "캐시 읽기가 전체의 94%를 차지. 실제 과금은 10배 할인 적용."
)

gemini_donut = make_donut(
    "Gemini", "gemini", 672522,
    [
        ("캐시 (555,670)",   555670/672522, "#93C5FD"),
        ("입력 (106,847)",   106847/672522, "#1A56DB"),
        ("추론 (4,122)",     4122/672522,   "#7C3AED"),
        ("출력 (3,521)",     3521/672522,   "#10B981"),
        ("라우터 (2,362)",   2362/672522,   "#F59E0B"),
    ],
    "캐시가 82.6%로 지배적. 2개 모델이 동시 동작."
)

# ── Section content ──────────────────────────────────────────

s1_table = data_table(
    [("비교 관점","left",""),("측정 항목","left","")],
    [
        ["토큰 사용량",  "각 API 호출의 입력 / 출력 / 캐시 토큰 수"],
        ["생성 속도",    "동일 작업에 소요되는 시간(초)"],
        ["코드 구조",    "파일 수, 라인 수, 패키지 구성, 아키텍처 선택"],
        ["UI 디자인",    "같은 명세에서 나오는 시각적 차이"],
        ["자동화 수준",  "빌드·오류 처리를 스스로 해결하는 정도"],
        ["한국어 처리",  "한국어 데이터를 올바르게 다루는 능력"],
    ],
)

s2_table = data_table(
    [("고정 조건","left",""),("값","left","")],
    [
        ["앱 이름",         "WeatherNow"],
        ["언어",            "Kotlin + Jetpack Compose"],
        ["아키텍처",        "MVVM (ViewModel + StateFlow)"],
        ["Mock 데이터",     "서울 / 부산 / 제주 (3개 AI 완전 동일)"],
        ["외부 라이브러리", "사용 금지"],
        ["프롬프트",        "3개 AI 모두 동일한 텍스트 파일 (prompt.txt)"],
        ["실행 환경",       "Windows 11 PowerShell — 비대화형 자동화 모드"],
    ],
)
s2_tags = '<div class="tag-row">' + "".join(
    f'<span class="tag">{t}</span>'
    for t in ["색상 테마 및 배경","레이아웃 구성","날씨 아이콘 스타일","카드/섹션 디자인","애니메이션 유무"]
) + "</div>"

# S03 — 실험 방법론
s3_script_table = data_table(
    [("스크립트","left",""),("대상 CLI","left",""),("실행 플래그","left",""),("특이사항","left","")],
    [
        ["01_run_claude.ps1","Claude Code CLI v2.1.152","--dangerously-skip-permissions -p","비대화형 모드; 권한 프롬프트 우회"],
        ["02_run_codex.ps1", "OpenAI Codex CLI v0.134.0","--dangerously-bypass-approvals-and-sandbox","stdin 파이프로 한국어 인코딩 보존"],
        ["03_run_gemini.ps1","Gemini CLI v0.43.0","--yolo","PTY 오류 발생하나 파일 생성 정상"],
    ],
)

s3_token_table = data_table(
    [("AI","left",""),("토큰 측정 방법","left",""),("소요 시간 측정","left","")],
    [
        ["Claude", "~/.claude/projects/.../session.jsonl JSONL 파싱 — API 호출 32회의 토큰 누적", "PowerShell (Get-Date) 실측 — 537초"],
        ["Codex",  "CLI 출력 로그에서 'tokens used: X' 텍스트 파싱 — 합계만 제공",              "로그에 시간 미포함 → 측정 불가"],
        ["Gemini", "--output-format json 플래그로 별도 재실행 후 JSON 파싱",                      "PowerShell (Get-Date) 실측 — 318초"],
    ],
)

# S04 — 프롬프트 설계
prompt_key = """# WeatherNow Android App Specification

## Tech Stack (Fixed)
- Language: Kotlin  |  UI: Jetpack Compose
- Architecture: MVVM (ViewModel + UiState)
- minSdk: 26  |  targetSdk: 35
- No external libraries  |  No real API — Mock data only
- App name: WeatherNow  |  Package: com.example.weathernow

## Required Information to Display
1. City name                4. Feels like temperature
2. Current temperature (°C) 5. Humidity (%)
3. Weather condition        6. Wind speed (m/s)
7. 5-day forecast (day + weather icon + high/low temp)

## City Switching
Seoul(서울) / Busan(부산) / Jeju(제주) — tabs or selector UI

## Free to Decide (AI's Design Style)
- Color theme and background    - Weather icon style
- Layout composition            - Card / section design
- Animations (optional)         - Font sizes and hierarchy"""

cli_commands = [
    ("Claude Code CLI",
     "# prompt.txt 내용을 인수로 전달\n"
     '$prompt = Get-Content "prompt.txt" -Raw\n'
     "claude --dangerously-skip-permissions -p $prompt"),
    ("OpenAI Codex CLI",
     "# stdin 파이프로 전달 (한국어 인코딩 보존)\n"
     'Get-Content "prompt.txt" -Encoding utf8 |\n'
     "  codex exec --dangerously-bypass-approvals-and-sandbox -C . -"),
    ("Gemini CLI",
     "# 변수에 담아 --yolo 플래그와 함께 전달\n"
     '$prompt = Get-Content "prompt.txt" -Raw\n'
     "gemini -p $prompt --yolo"),
]

# S05 — 수치 비교 + 종합 점수
score_rows = [
    ["생성 속도",    "소요 시간 (짧을수록 ↑)", score_bar(2), score_bar(3), score_bar(5)],
    ["토큰 효율",    "실질 과금 환산 기준",     score_bar(3), score_bar(5), score_bar(4)],
    ["명세 준수",    "요구사항 충족 정확도",     score_bar(4), score_bar(3), score_bar(3)],
    ["자동화 수준",  "빌드/APK 자동 처리",      score_bar(2), score_bar(5), score_bar(3)],
    ["한국어 처리",  "한국어 데이터 정확성",    score_bar(5), score_bar(1), score_bar(5)],
    ["UI 완성도",    "UI 품질 및 완성도",        score_bar(5), score_bar(2), score_bar(4)],
    ["합계 / 30",   "종합 점수",
     '<strong style="color:#C2622A">21점 (70%)</strong>',
     '<strong style="color:#0D8A68">19점 (63%)</strong>',
     '<strong style="color:#1A56DB">24점 (80%)</strong>'],
]
score_table = (
    '<table class="score-table">'
    '<thead><tr>'
    '<th>평가 기준</th><th>설명</th>'
    '<th style="color:#C2622A">Claude</th>'
    '<th style="color:#0D8A68">Codex</th>'
    '<th style="color:#1A56DB">Gemini</th>'
    '</tr></thead><tbody>'
    + "".join(
        "<tr>" + "".join(
            f'<td class="col-label">{v}</td>' if j==0 else f"<td>{v}</td>"
            for j,v in enumerate(row)
        ) + "</tr>"
        for row in score_rows
    )
    + '</tbody></table>'
)

s5_main_table = data_table(
    [("항목","left",""),("Claude","center","c-claude"),("Codex","center","c-codex"),("Gemini","center","c-gemini")],
    [
        ["사용 모델",
         '<span class="num" style="font-size:12px">claude-sonnet-4-6</span>',
         '<span class="num" style="font-size:12px">gpt-5.5</span>',
         '<span class="num" style="font-size:12px">gemini-3-flash-preview</span>'],
        ["생성 소요 시간",      "537초 (9분)",  '<span class="c-gray">— 측정 불가</span>', '<span class="ok">318초 (5.3분)</span>'],
        ["KT 파일 수",          "5개",  "<strong>6개</strong>",   "5개"],
        ["KT 코드 라인",        "395줄","<strong>451줄</strong>",  "304줄"],
        ["빌드 성공",           '<span class="ok">✔</span>', '<span class="ok">✔</span>', '<span class="ok">✔</span>'],
        ["APK 자동 빌드",       '<span class="no">✘</span>', '<span class="ok">✔ 자동</span>', '<span class="no">✘</span>'],
        ["한국어 처리",         '<span class="ok">✔ 정상</span>', '<span class="no">✘ 깨짐</span>', '<span class="ok">✔ 정상</span>'],
        ["실질 과금 환산 토큰 ★", '<strong class="num">약 305,082</strong>', '<span class="c-gray">산출 불가</span>', '<strong class="num">약 278,699</strong>'],
    ],
    row_highlights={7},
)

# S06 — 토큰
s6_cards = (
    token_card("claude","Claude","claude-sonnet-4-6 · Claude Code CLI v2.1.152",[
        ("신규 입력 토큰",   "40",         False),
        ("캐시 생성 토큰",   "12,305",     False),
        ("캐시 읽기 토큰",   "865,307",    False),
        ("출력 토큰",        "40,626",     False),
        ("Raw 합계",         "918,278",    True),
        ("실질 과금 환산",   "약 305,082", True),
    ],"캐시 읽기 10배 할인 적용. Raw 대비 <strong>67% 비용 절감</strong>.")
    + token_card("codex","Codex","gpt-5.5 · Codex CLI v0.134.0",[
        ("총 토큰 (합계)", "165,761",    True),
        ("입력/출력 분리", "CLI 미제공", False),
        ("소요 시간",      "측정 불가",  False),
        ("실질 과금 환산", "산출 불가",  False),
    ],"Raw 기준 3개 중 <strong>최소 토큰</strong>. 입출력 분리 없어 환산 불가.")
    + token_card("gemini","Gemini","gemini-3-flash-preview · Gemini CLI v0.43.0",[
        ("입력",             "106,847",    False),
        ("출력",             "3,521",      False),
        ("캐시",             "555,670",    False),
        ("추론(Thinking)",   "4,122",      False),
        ("라우터(flash-lite)","2,362",     False),
        ("Raw 합계",         "672,522",    True),
        ("실질 과금 환산",   "약 278,699", True),
    ],"2개 모델 동시 운용. <strong>59% 비용 절감</strong>.")
)

s6_calc = data_table(
    [("토큰 유형","left",""),("가중치","center",""),("Claude","center","c-claude"),("Gemini","center","c-gemini")],
    [
        ["일반 입력",  "× 1.0",              "40",                                   "106,847"],
        ["캐시 생성",  "× 1.25",             "12,305 → <strong>15,381</strong>",     "—"],
        ["캐시 읽기",  "× 0.1 / × 0.25",    "865,307 → <strong>86,531</strong>",    "555,670 → <strong>138,918</strong>"],
        ["출력",       "× 5.0 / × 4.0",     "40,626 → <strong>203,130</strong>",    "3,521 → <strong>14,084</strong>"],
        ["추론",       "× 4.0",              "—",                                    "4,122 → <strong>16,488</strong>"],
        ["라우터",     "× 1.0",              "—",                                    "2,362"],
        ["환산 합계",  "—",                  "<strong>약 305,082</strong>",           "<strong>약 278,699</strong>"],
        ["Raw 대비 절감","—",               '<span class="ok">↓ 67%</span>',         '<span class="ok">↓ 59%</span>'],
    ],
    row_highlights={6,7},
)

s6_bars_raw = (
    '<p class="div-label">Raw 총합 비교</p>'
    + hbar("Claude","claude","918,278",100)
    + hbar("Gemini","gemini","672,522",73)
    + hbar("Codex","codex","165,761",18)
)
s6_bars_eff = (
    '<p class="div-label" style="margin-top:18px">실질 과금 환산 비교 (Claude · Gemini)</p>'
    + hbar("Claude","claude","약 305,082",100)
    + hbar("Gemini","gemini","약 278,699",91)
    + '<p style="font-size:12px;color:#BBB;margin-top:4px">Codex 입출력 분리 불가로 환산 제외 — Raw 165,761 토큰</p>'
)

# S07 — UI
s7_ss = (
    '<div class="ss-grid">'
    + ss_card("claude","Claude",c_img)
    + ss_card("codex","Codex",d_img)
    + ss_card("gemini","Gemini",g_img)
    + '</div>'
)
s7_table = data_table(
    [("UI 요소","left",""),("Claude","center","c-claude"),("Codex","center","c-codex"),("Gemini","center","c-gemini")],
    [
        ["배경 테마","라이트 / 하늘색 그라데이션","다크 / 네이비",'<span class="ok">라이트 / 인디고</span>'],
        ["도시 선택","상단 Pill 탭","상단 버튼 탭","상단 탭"],
        ["날씨 아이콘","이모지 (대형)",'<span class="no">이모지 (깨짐 ??)</span>',"이모지"],
        ["상세 정보","카드 3개","2열 배치","카드형"],
        ["5일 예보","세로 리스트","세로 리스트","세로 리스트"],
    ],
)

# S08 — 코드
s8_blocks = (
    code_block("claude","Claude",[
        ("파일 구조","data/ · ui/ · ui/theme/ · viewmodel/ (역할별 분리)"),
        ("Gradle",   "libs.versions.toml 버전 카탈로그 — 최신 방식"),
        ("UI",       "WeatherScreen.kt 단일 파일에 전체 UI"),
        ("추가",     "enableEdgeToEdge() — edge-to-edge 지원"),
        ("단점",     "gradlew · gradle-wrapper.jar 미생성"),
    ])
    + code_block("codex","Codex",[
        ("파일 구조","ui/ · ui/components/ (ForecastRow, MetricTile 분리)"),
        ("자동화",   "생성 완료 후 gradlew assembleDebug 자동 실행"),
        ("UI",       "재사용 컴포저블을 components/ 패키지로 분리"),
        ("단점 1",   "lifecycle-viewmodel-ktx 외부 라이브러리 추가 (명세 위반)"),
        ("단점 2",   "한국어 인코딩 깨짐 — ?? 로 표시"),
    ])
    + code_block("gemini","Gemini",[
        ("파일 구조","ui/ · ui/components/WeatherComponents.kt"),
        ("특징",     "3개 중 가장 적은 코드 (304줄) 로 구현"),
        ("UI",       "WeatherScreen + WeatherComponents 2파일 분리"),
        ("추가",     "빌드 검증 후 완료 메시지 출력"),
        ("단점",     "mipmap 리소스 누락 → 수동 보완 필요"),
    ])
)

# S09 — 장단점
s9_blocks = (
    pc_block("claude","Claude",
        ["최신 Gradle 방식 (libs.versions.toml)","날씨별 동적 배경색 등 세련된 UI","한국어 처리 정상","캐시로 실질 비용 67% 절감"],
        ["gradlew/jar 미생성 (수동 보완)","비대화형 모드 권한 요청","단일 파일 UI — 컴포넌트 분리 미흡"]
    )
    + pc_block("codex","Codex",
        ["생성 후 자동 빌드 → APK 원스톱","컴포넌트 별도 파일 분리","Raw 기준 최소 토큰 (165,761)","가장 많은 코드 (451줄) 상세 구현"],
        ["한국어 인코딩 깨짐 (stdin 파이프 한계)","외부 라이브러리 추가 (명세 위반)","입출력 토큰 분리 미제공","소요 시간 측정 불가"]
    )
    + pc_block("gemini","Gemini",
        ["가장 빠른 생성 (318초)","간결한 코드 (304줄)","2개 모델 협력 구조","한국어 처리 정상"],
        ["mipmap 리소스 누락 (빌드 실패)","PTY 오류 발생 (비대화형 모드)","Thinking 토큰 별도 비용"]
    )
)

# S10 — 결론
s10_recs = (
    '<div class="rec-grid">'
    + rec("claude","코드 완성도 + 최신 패턴","Claude","최신 Gradle 방식, 세련된 UI, 캐시로 비용 효율적")
    + rec("codex","빠른 프로토타입 + 자동화","Codex","자동 빌드·APK 원스톱, Raw 기준 최소 토큰")
    + rec("gemini","속도 우선 + 간결함","Gemini","가장 빠른 생성, 간결한 코드, Thinking 추론")
    + "</div>"
)
s10_insights = (
    insight("💡","토큰 수치만으로 비용 비교는 오해를 낳습니다. Claude 캐시 읽기(×0.1)와 Gemini Thinking 토큰은 과금 방식이 전혀 다릅니다.")
    + insight("⚡","Codex는 165,761 Raw 토큰으로 APK까지 완성했지만, 한국어 처리 실패가 한국어 프로젝트에서 치명적입니다.")
    + insight("🎨","3개 AI 모두 같은 요구사항을 충족했지만 UI 디자인은 완전히 달랐습니다. AI에게도 고유한 '디자인 개성'이 있습니다.")
    + insight("🔀","실질 과금 환산 기준 Claude(305K)와 Gemini(279K)는 비슷하지만 Gemini가 318초로 40% 빠릅니다.")
    + insight("🔬","이 실험의 한계: Codex 소요시간 미측정, Gemini 토큰은 재실행 데이터, 평가 점수는 주관적 5점 척도입니다.")
)

# S11 — AI 구독 서비스 비교 (Kotlin / Flutter 개발팀)
s11_summary = (
    '<div class="sub-metrics">'
    + '<div class="sub-metric">'
    + '<div class="sub-metric-label">매일 쓰는 자동완성</div>'
    + '<div class="sub-metric-value">GitHub Copilot<br>JetBrains AI</div>'
    + '<div class="sub-metric-desc">IDE 안에서 바로 코드 제안. Kotlin/Android Studio 개발은 JetBrains AI가 특히 자연스럽다.</div>'
    + '</div>'
    + '<div class="sub-metric">'
    + '<div class="sub-metric-label">AI 기반 코드 수정</div>'
    + '<div class="sub-metric-value">Cursor<br>ChatGPT / Codex</div>'
    + '<div class="sub-metric-desc">Flutter 프로젝트처럼 여러 파일을 함께 수정하거나 기능 구현을 맡길 때 유리하다.</div>'
    + '</div>'
    + '<div class="sub-metric">'
    + '<div class="sub-metric-label">리뷰·디버깅·문서화</div>'
    + '<div class="sub-metric-value">Claude<br>ChatGPT</div>'
    + '<div class="sub-metric-desc">긴 코드 이해, 리팩토링, 에러 원인 분석, 개발 문서 작성에 강점이 있다.</div>'
    + '</div>'
    + '</div>'
)

s11_comp_table = data_table(
    [("서비스","left",""),("Kotlin","center",""),("Flutter","center",""),
     ("코드생성","center",""),("코드리뷰","center",""),("디버깅","center",""),
     ("IDE연동","center",""),("보안부담","center",""),("한 줄 평가","left","")],
    [
        ["ChatGPT / Codex","높음","높음","매우 높음","높음","매우 높음","중간",rt("low","낮음~중간"),"범용 개발 보조와 코딩 작업에 모두 활용 가능"],
        ["Claude / Claude Code","높음","높음","높음","매우 높음","높음","중간",rt("low","낮음~중간"),"코드 리뷰, 리팩토링, 긴 코드 이해에 강함"],
        ["Gemini","중간~높음","중간~높음","중간~높음","중간","중간~높음","중간",rt("low","낮음~중간"),"Google 생태계와 문서 기반 업무에 적합"],
        ["GitHub Copilot","높음","높음","높음","중간","중간","매우 높음",rt("low","낮음~중간"),"매일 쓰기 좋은 코드 자동완성 도구"],
        ["Cursor","중간","매우 높음","매우 높음","높음","높음","높음",rt("med","중간"),"Flutter/VS Code 기반 AI 코드 에디터"],
        ["JetBrains AI","매우 높음","중간","높음","높음","높음","매우 높음",rt("low","낮음~중간"),"Kotlin/Android Studio 개발에 가장 자연스러움"],
        ["중국계 AI (Qwen/DeepSeek 등)","중간~높음","중간~높음","높음","중간","중간","낮음",rt("hi","높음"),"비용 효율은 좋지만 보안 검토 필요"],
    ]
)

s11_benchmarks = (
    '<div class="two-col" style="margin:14px 0">'
    + '<div style="border:1px solid #E2E2E2;border-radius:10px;padding:22px 24px">'
    + '<p style="font-weight:800;font-size:15px;margin:0 0 4px">LLM Stats Coding Score</p>'
    + '<p style="font-size:13px;color:#9CA3AF;margin:0 0 16px">모델별 코딩 지표 비교. 점수는 리더보드 갱신에 따라 변동될 수 있다.</p>'
    + bench_bar("GPT-5.5", 100, "51.1")
    + bench_bar("Claude Opus 4.7", 98, "50.2")
    + bench_bar("Qwen3.7 Max", 96, "48.9")
    + bench_bar("Gemini 3.5 Flash", 91, "46.3")
    + bench_bar("Kimi K2.6", 87, "44.5")
    + '</div>'
    + '<div style="border:1px solid #E2E2E2;border-radius:10px;padding:22px 24px">'
    + '<p style="font-weight:800;font-size:15px;margin:0 0 4px">Terminal-Bench 2.0</p>'
    + '<p style="font-size:13px;color:#9CA3AF;margin:0 0 16px">터미널 기반 개발 작업, 테스트 실행, 환경 문제 해결 능력을 평가한다.</p>'
    + bench_bar("Claude Opus 4.7", 100, "90.2%")
    + bench_bar("GPT-5.5", 94, "84.7%")
    + bench_bar("Codex CLI / GPT-5.5", 91, "82.0%")
    + bench_bar("Gemini 3.1 Pro", 89, "80.2%")
    + bench_bar("GPT-5.3 Codex", 87, "78.4%")
    + bench_bar("JetBrains Junie CLI", 79, "71.0%")
    + '</div>'
    + '</div>'
)

s11_bench_table = data_table(
    [("벤치마크","left",""),("평가 내용","left",""),("개발팀 관점 의미","left","")],
    [
        ["SWE-bench","실제 GitHub 이슈 해결 능력","코드 수정, 버그 해결, 레포지토리 기반 작업 능력 확인"],
        ["Terminal-Bench","터미널 명령, 테스트, 환경 문제 해결","Gradle, Flutter build, CLI 오류 해결 능력과 연결"],
        ["LLM Stats Coding","모델별 코딩 지표 비교","구독 후보 모델의 코딩 성능을 빠르게 비교하기 좋음"],
    ]
)

s11_services = (
    '<div class="pc-cards">'
    + svc_pc_block("ChatGPT / Codex","All-rounder",
        ["코드 생성, 디버깅, 문서화까지 범용성이 좋음","Kotlin, Flutter, 백엔드, 배포 질문까지 처리 가능","복잡한 에러 원인 분석과 테스트 코드 작성에 유리"],
        ["IDE 자동완성은 Copilot·JetBrains AI보다 직접성이 낮을 수 있음","사용량이 많으면 상위 플랜 검토 필요"]
    )
    + svc_pc_block("Claude / Claude Code","Review",
        ["긴 코드와 문서 이해에 강함","PR 리뷰, 리팩토링, 구조 개선 제안에 적합","Terminal-Bench 계열에서 강한 성능 (90.2%)"],
        ["단순 자동완성용으로는 과할 수 있음","사용량 제한과 비용 고려 필요"]
    )
    + svc_pc_block("Gemini","Google",
        ["긴 컨텍스트와 빠른 응답이 장점","Android/Google 생태계와 함께 쓰기 좋음","문서 요약, 요구사항 분석, Workspace 업무에 유리"],
        ["코딩 전문 도구로는 Codex·Claude·Cursor 대비 검증 필요","Flutter/Dart 품질은 실제 프로젝트 테스트 권장"]
    )
    + svc_pc_block("GitHub Copilot","Autocomplete",
        ["IDE 안에서 매일 쓰기 가장 무난함","VS Code, JetBrains IDE, GitHub 연동성이 좋음","반복 코드와 테스트 코드 초안 작성에 유리"],
        ["깊은 설계 판단이나 긴 코드 리뷰는 전문 모델보다 약할 수 있음","자동완성 결과는 개발자 검토가 필수"]
    )
    + svc_pc_block("Cursor","Flutter",
        ["다중 파일 수정과 리팩토링에 강함","Flutter/VS Code 기반 프로젝트와 잘 맞음","자연어로 기능 구현을 요청하기 좋음"],
        ["Android Studio 중심 Kotlin 개발은 완전 대체 어려움","기존 IDE 전환 부담이 있음"]
    )
    + svc_pc_block("JetBrains AI","Kotlin",
        ["IntelliJ·Android Studio 환경과 잘 맞음","Kotlin 코드 생성, 설명, 리팩토링에 자연스러움","Android 개발자에게 IDE 전환 부담이 낮음"],
        ["VS Code 중심 Flutter 팀에는 우선순위가 낮을 수 있음","모델 성능보다 IDE 통합 경험이 핵심 장점"]
    )
    + svc_pc_block("중국계 AI (Qwen / DeepSeek / Kimi / MiMo)","Security ⚠",
        ["가격 대비 성능이 좋음","일부 코딩 벤치마크에서 강한 후보로 등장","로그 요약, 이슈 분류, 대량 자동화에 비용 효율적"],
        ["중국 기업이 만든 AI이므로 보안/법무 검토 필요","회사 소스코드와 고객 데이터 입력 전 정책 확인 필수","고객사나 산업군에 따라 사용이 민감할 수 있음"]
    )
    + '</div>'
)

s11_matrix = (
    '<div class="tbl-wrap"><table>'
    + '<thead><tr>'
    + '<th>업무</th>'
    + '<th style="text-align:center">ChatGPT/<br>Codex</th>'
    + '<th style="text-align:center">Claude</th>'
    + '<th style="text-align:center">Gemini</th>'
    + '<th style="text-align:center">Copilot</th>'
    + '<th style="text-align:center">Cursor</th>'
    + '<th style="text-align:center">JetBrains<br>AI</th>'
    + '<th style="text-align:center">중국계<br>AI</th>'
    + '</tr></thead><tbody>'
    + matrix_row("Kotlin 코드 자동완성",     "m","l","l","h","l","h","l")
    + matrix_row("Flutter 코드 자동완성",    "m","l","l","h","h","m","l")
    + matrix_row("Flutter 화면 구현",        "h","m","l","m","h","l","l")
    + matrix_row("Kotlin Android 구조 설계", "h","h","m","l","l","h","l")
    + matrix_row("코드 리뷰",               "h","h","m","m","m","m","l")
    + matrix_row("리팩토링",               "h","h","l","m","h","m","l")
    + matrix_row("빌드 에러 분석",          "h","h","m","l","m","m","l")
    + matrix_row("문서화",                 "h","h","h","l","l","l","l")
    + matrix_row("대량 로그/이슈 분류",      "m","m","m","l","l","l","h")
    + '</tbody></table></div>'
    + '<div style="margin:12px 0;font-size:13px;color:#6B7280;display:flex;gap:18px;align-items:center">'
    + f'<span>{sb("h")} 매우 적합</span>'
    + f'<span>{sb("m")} 적합</span>'
    + f'<span>{sb("l")} 제한적</span>'
    + '</div>'
)

s11_security = data_table(
    [("구분","left",""),("위험도","center",""),("확인할 것","left","")],
    [
        ["ChatGPT / OpenAI",   rt("low","낮음~중간"), "기업 플랜, 데이터 학습 사용 여부, 관리자 제어 기능 확인"],
        ["Claude / Anthropic", rt("low","낮음~중간"), "팀 플랜, 코드 입력 정책, 사용량 제한 확인"],
        ["Gemini / Google",    rt("low","낮음~중간"), "Google Workspace/Cloud 정책과 함께 검토"],
        ["GitHub Copilot",     rt("low","낮음~중간"), "조직 정책, 코드 제안 데이터 처리, Business/Enterprise 설정 확인"],
        ["Cursor",             rt("med","중간"),      "코드베이스 인덱싱, 프라이버시 모드, 팀 정책 확인"],
        ["JetBrains AI",       rt("low","낮음~중간"), "JetBrains AI 플랜, 데이터 처리, 조직 계정 정책 확인"],
        ["중국계 AI 모델",     rt("hi","높음"),       "회사 코드, 고객 데이터, 운영 로그 입력 전 보안/법무 검토 필수"],
    ]
)

s11_checklist = (
    '<div class="check-grid">'
    + check_item("Flutter 화면 구현", "디자인 요구사항을 위젯 구조로 자연스럽게 구현하는지 확인")
    + check_item("Dart 상태관리", "Provider, Riverpod, Bloc 등 팀 표준 구조에 맞는지 확인")
    + check_item("Kotlin ViewModel", "MVVM 구조와 Coroutine 처리가 적절한지 확인")
    + check_item("Jetpack Compose", "Compose UI 코드가 재사용성과 가독성을 갖추는지 확인")
    + check_item("API 연동", "Retrofit, Dio, 인증 헤더, 예외 처리를 잘 작성하는지 확인")
    + check_item("에러 분석", "Gradle, Flutter build, pub dependency 오류를 해결하는지 확인")
    + check_item("PR 코드 리뷰", "실제 PR에서 구조 문제, 버그 가능성, 보안 이슈를 찾는지 확인")
    + check_item("문서화", "README, API 문서, 장애 회고를 읽기 쉽게 정리하는지 확인")
    + '</div>'
)

s11_sources = (
    '<div class="src-list">'
    + src_item("1","LLM Stats","300개 이상 모델의 LLM Stats Score, Coding, Agent, Speed, Pricing 비교","https://llm-stats.com/")
    + src_item("2","SWE-bench","실제 GitHub 이슈 해결 능력 평가 — Verified, Multilingual, Lite, Full 리더보드","https://www.swebench.com/")
    + src_item("3","Terminal-Bench 2.0","터미널 기반 문제 해결 능력 평가 — Claude Opus 4.7, GPT-5.5, Codex CLI 등 순위","https://www.tbench.ai/leaderboard/terminal-bench/2.0")
    + src_item("4","Artificial Analysis","모델별 지능, 가격, 속도, 지연시간, 컨텍스트 길이 비교 리더보드","https://artificialanalysis.ai/leaderboards/models")
    + src_item("5","제품/구독 가격 공식 페이지","ChatGPT, Claude, Gemini, GitHub Copilot, Cursor, JetBrains AI — 각 공식 가격 페이지에서 플랜·사용량·팀 기능 확인")
    + '</div>'
)

# ── TOC ─────────────────────────────────────────────────────
toc_html = '<nav class="toc">' + "".join(
    f'<a href="{h}">{t}</a>'
    for h,t in [
        ("#s11","01 AI 서비스 비교"),("#s1","02 기획 의도"),("#s2","03 실험 설계"),("#s3","04 방법론"),
        ("#s4","05 프롬프트"),("#s5","06 수치 비교"),("#s6","07 토큰 분석"),
        ("#s7","08 UI 비교"),("#s8","09 코드 분석"),("#s9","10 장단점"),("#s10","11 결론"),
    ]
) + "</nav>"

# ── Final HTML ───────────────────────────────────────────────
html = f"""<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1.0">
<title>AI 코딩 도구 비교 리포트 — Claude vs Codex vs Gemini</title>
<style>{css}</style>
</head>
<body id="top">
<div class="wrap">

<header class="rh">
  <p class="rh-eyebrow">AI 코딩 도구 비교 실험 · 2026년 5월</p>
  <h1 class="rh-title">Claude &nbsp;vs&nbsp; Codex &nbsp;vs&nbsp; Gemini</h1>
  <p class="rh-sub">동일한 Android 날씨 앱을 3개의 AI CLI에게 제공했을 때<br>토큰, 속도, 코드 품질, UI 디자인이 어떻게 달라지는가</p>
  <div class="rh-meta">
    <span class="ai-pill"><span class="dot claude"></span>Claude Sonnet 4.6</span>
    <span class="ai-pill"><span class="dot codex"></span>OpenAI Codex (gpt-5.5)</span>
    <span class="ai-pill"><span class="dot gemini"></span>Gemini 3-flash-preview</span>
    <span class="tag-sm">WeatherNow Android App · Kotlin + Compose</span>
  </div>
</header>

{toc_html}

<div class="card" id="s11">
  <div class="sh"><span class="sh-num">01</span><h2 class="sh-title">AI 구독 서비스 비교 — Kotlin / Flutter 개발팀</h2></div>
  <p class="intro">
    Claude, Codex, Gemini 외에도 개발팀이 실무에서 사용할 수 있는 AI 구독 서비스를
    Kotlin·Flutter 개발 기준으로 비교했습니다. 자동완성, 코드 수정, 리뷰/디버깅 등
    업무 유형별로 어떤 도구가 적합한지 정리했습니다.
  </p>
  <p class="div-label" style="margin-top:0">한 장 요약 — 용도별 추천</p>
  {s11_summary}
  <p class="div-label">전체 비교표 (7개 서비스 × 8개 기준)</p>
  {s11_comp_table}
  <p class="div-label">코딩 벤치마크</p>
  {s11_benchmarks}
  <p class="div-label">벤치마크 해석</p>
  {s11_bench_table}
  <p class="div-label">서비스별 장단점</p>
  {s11_services}
  <p class="div-label">Kotlin / Flutter 업무별 매트릭스</p>
  {s11_matrix}
  <p class="div-label">보안 리스크</p>
  {s11_security}
  {note_box("소스코드나 고객 데이터를 AI 서비스에 입력할 경우 반드시 최신 정책을 확인하세요. 특히 중국계 AI 모델은 보안/법무 검토가 필수입니다.")}
  <p class="div-label">구독 전 팀 테스트 체크리스트</p>
  {s11_checklist}
  <p class="div-label">참고 출처</p>
  {s11_sources}
</div>

<div class="card" id="s1">
  <div class="sh"><span class="sh-num">02</span><h2 class="sh-title">프로젝트 기획 의도</h2></div>
  <p class="intro">
    AI 코딩 도구가 빠르게 확산되면서 개발자들은 Claude, Codex, Gemini 중 어떤 도구를 선택해야
    할지 판단하기 어렵습니다. 마케팅 자료가 아닌 <strong>실제 실험 데이터</strong>로 차이를 비교합니다.
  </p>
  {callout("<strong>핵심 질문 —</strong> 같은 앱을 만들 때 토큰 비용, 속도, 코드 품질, UI 디자인이 AI마다 얼마나 다른가?")}
  {s1_table}
</div>

<div class="card" id="s2">
  <div class="sh"><span class="sh-num">03</span><h2 class="sh-title">실험 설계</h2></div>
  <p class="div-label" style="margin-top:0">고정 조건 — 3개 AI 동일</p>
  {s2_table}
  <p class="div-label">자유 조건 — AI 개성 발휘 영역</p>
  {s2_tags}
</div>

<div class="card" id="s3">
  <div class="sh"><span class="sh-num">04</span><h2 class="sh-title">실험 방법론</h2></div>
  <p class="intro">
    3개 AI 모두 PowerShell 스크립트로 비대화형(non-interactive) 자동화 모드로 실행했습니다.
    동일한 <code style="background:#F3F4F6;padding:2px 7px;border-radius:4px;font-size:13px;font-family:Consolas,monospace">prompt.txt</code>를
    읽어 각 CLI에 전달하고, 결과 파일·토큰·시간을 자동으로 기록합니다.
  </p>
  <p class="div-label" style="margin-top:0">스크립트 구성</p>
  {s3_script_table}
  <p class="div-label">CLI 실행 명령어</p>
  {"".join(snippet(title, code) for title, code in cli_commands)}
  <p class="div-label">토큰 및 시간 측정 방법</p>
  {s3_token_table}
  {note_box("Codex CLI는 로그에 시간을 기록하지 않아 소요 시간 측정이 불가능했습니다. Gemini 토큰은 기존 프로젝트에서 재측정한 값으로, 초기 생성 시보다 출력 토큰이 적을 수 있습니다.")}
</div>

<div class="card" id="s4">
  <div class="sh"><span class="sh-num">05</span><h2 class="sh-title">프롬프트 설계</h2></div>
  <p class="intro">
    3개 AI에 <strong>완전히 동일한 프롬프트</strong>를 제공했습니다. 파일 경로를 넘기면 각 AI의 샌드박스 제한으로
    접근이 차단될 수 있어, 명세 전체를 <strong>인라인 텍스트</strong>로 직접 전달했습니다.
  </p>
  {snippet("prompt.txt — 핵심 명세 (일부)", prompt_key)}
  <p class="div-label" style="margin-top:16px">날씨 앱을 선택한 이유</p>
  <div class="two-col">
    {callout("<strong>UI 다양성 확보</strong><br>날씨 앱은 디자인 자유도가 높아 AI마다 색상·레이아웃·아이콘 선택이 크게 다를 것으로 예상")}
    {callout("<strong>명확한 데이터 구조</strong><br>Mock 데이터를 완전히 동일하게 고정해 '데이터가 달라서 결과가 달랐다'는 변수를 제거")}
  </div>
  <div class="two-col">
    {callout("<strong>한국어 처리 검증</strong><br>한글 도시명과 날씨 상태를 Mock 데이터에 포함해 한국어 처리 능력을 자연스럽게 테스트")}
    {callout("<strong>빌드 결과 검증 가능</strong><br>Android APK 빌드 성공 여부로 '실제로 동작하는 코드'인지 객관적으로 확인 가능")}
  </div>
</div>

<div class="card" id="s5">
  <div class="sh"><span class="sh-num">06</span><h2 class="sh-title">수치 비교 및 종합 평가</h2></div>
  <p class="div-label" style="margin-top:0">6개 기준 종합 점수 (각 5점 만점 · 주관적 평가)</p>
  {score_table}
  {note_box("점수는 이 실험 결과를 기반으로 한 주관적 평가입니다. 속도 측정 불가인 Codex는 APK 자동 완성 고려해 3점 부여.")}
  <p class="div-label">레이더 차트 — 6개 기준 시각 비교</p>
  <div style="margin:16px 0">{radar}</div>
  <p class="div-label">수치 요약표</p>
  {s5_main_table}
  {note_box("★ 실질 과금 환산 토큰 = 토큰 유형별 가격 가중치 적용값 (캐시읽기×0.1, 캐시생성×1.25, 출력×5.0). Raw 총합이 아닌 실제 비용 규모를 비교하는 지표.")}
</div>

<div class="card" id="s6">
  <div class="sh"><span class="sh-num">07</span><h2 class="sh-title">토큰 사용량 상세 분석</h2></div>
  <div class="token-cards">{s6_cards}</div>
  <p class="div-label">토큰 유형 분포 — 도넛 차트</p>
  <div class="two-col" style="margin:14px 0">
    {claude_donut}
    {gemini_donut}
  </div>
  <div style="border:1px solid #E2E2E2;border-radius:8px;overflow:hidden">
    <div style="display:flex;align-items:center;gap:9px;padding:9px 16px;background:#FAFAFA;border-bottom:1px solid #E8E8E8">
      <span class="dot codex"></span><span style="font-weight:700;font-size:13.5px">Codex</span>
    </div>
    <div style="padding:16px 20px;font-size:13.5px;color:#888;line-height:1.8">
      Codex CLI는 입력/출력 토큰을 분리하지 않고 합계(<strong style="color:#333">165,761</strong>)만 제공합니다.
      따라서 유형별 분포 차트를 생성할 수 없습니다.
    </div>
  </div>
  <p class="div-label">실질 과금 환산 계산 상세</p>
  {s6_calc}
  {note_box("Codex는 입출력 분리 없어 환산 불가. Raw 합계 165,761이 전부.")}
  <div style="margin-top:22px">
    {s6_bars_raw}
    {s6_bars_eff}
  </div>
</div>

<div class="card" id="s7">
  <div class="sh"><span class="sh-num">08</span><h2 class="sh-title">UI 스크린샷 비교</h2></div>
  <p class="intro">동일한 Mock 데이터(서울, 23°C, 맑음)를 표시하는 화면. 프롬프트는 동일하지만 UI 디자인은 완전히 다르게 나타남.</p>
  {s7_ss}
  <p class="div-label">UI 특징 비교</p>
  {s7_table}
</div>

<div class="card" id="s8">
  <div class="sh"><span class="sh-num">09</span><h2 class="sh-title">코드 스타일 분석</h2></div>
  <p class="div-label" style="margin-top:0">코드 라인 수 비교</p>
  {hbar("Claude","claude","395줄",88)}
  {hbar("Codex","codex","451줄",100)}
  {hbar("Gemini","gemini","304줄",67)}
  <div style="margin-top:20px" class="code-cards">{s8_blocks}</div>
</div>

<div class="card" id="s9">
  <div class="sh"><span class="sh-num">10</span><h2 class="sh-title">AI별 장점과 단점</h2></div>
  <div class="pc-cards">{s9_blocks}</div>
</div>

<div class="card" id="s10">
  <div class="sh"><span class="sh-num">11</span><h2 class="sh-title">결론 및 용도별 추천</h2></div>
  <p class="intro">
    3개의 AI 모두 같은 명세로 동작하는 Android 앱을 완성했습니다.
    하지만 속도, 비용, 코드 품질, UI에서 뚜렷한 차이를 보였습니다.
  </p>
  {s10_recs}
  <p class="div-label">핵심 인사이트</p>
  {s10_insights}
</div>

<footer>
  AI 코딩 도구 비교 실험 리포트 &nbsp;·&nbsp; 2026년 5월 &nbsp;·&nbsp; WeatherNow Android App
</footer>

<a href="#top" id="back-top" title="맨 위로">↑</a>

</div>
<script>
(function() {{
  var btn = document.getElementById('back-top');
  window.addEventListener('scroll', function() {{
    btn.classList.toggle('show', window.scrollY > 400);
  }}, {{ passive: true }});
}})();
</script>
</body>
</html>"""

with open(OUTPUT, "w", encoding="utf-8") as f:
    f.write(html)

size_mb = os.path.getsize(OUTPUT) / 1024 / 1024
print(f"저장 완료: {OUTPUT}")
print(f"파일 크기: {size_mb:.1f} MB")
