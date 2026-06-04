# -*- coding: utf-8 -*-
import re, sys
sys.stdout.reconfigure(encoding='utf-8')

BASE = 'C:/proj/weather-ai-compare/results/'

# V4, V5: remove baseline+result tags, clean generated: prefix, clean prompt-meta
# V6-V9: clean baseline: prefix to plain Korean label

fixes = {
    'report_v4.html': {
        'remove_tags': ['baseline: weather-v3', 'result: weather-v4'],
        'clean_generated': True,
        'prompt_meta_old': 'prompt_v4.txt · Open-Meteo 실시간 API 연동',
        'prompt_meta_new': 'AI에게 전달한 프롬프트 (영문)',
    },
    'report_v5.html': {
        'remove_tags': ['baseline: weather-v4', 'result: weather-v5 (tests)'],
        'clean_generated': True,
        'prompt_meta_old': 'prompt_v5.txt · 단위 테스트 작성',
        'prompt_meta_new': 'AI에게 전달한 프롬프트 (영문)',
    },
    'report_v6.html': {
        'baseline_rename': ('baseline: V4 코드 + 버그 5개 주입', 'V4 코드 기반 · 버그 5개 포함'),
    },
    'report_v7.html': {
        'baseline_rename': ('baseline: V6 버그 수정 완료 코드', 'V6 완료 코드 기반'),
    },
    'report_v8.html': {
        'baseline_rename': ('baseline: V7 Room DB 완료 코드', 'V7 완료 코드 기반'),
    },
    'report_v9.html': {
        'baseline_rename': ('baseline: V8 WorkManager 완료 코드', 'V8 완료 코드 기반'),
    },
}

for fname, fix in fixes.items():
    path = BASE + fname
    with open(path, encoding='utf-8') as f:
        content = f.read()
    original = content

    # Remove tags completely
    for tag_text in fix.get('remove_tags', []):
        content = re.sub(
            rf'\s*<span class="tag-sm">{re.escape(tag_text)}</span>',
            '', content
        )

    # Clean "generated: " prefix
    if fix.get('clean_generated'):
        content = content.replace('generated: 2026-', '2026-')

    # Rename baseline tag text
    if 'baseline_rename' in fix:
        old_text, new_text = fix['baseline_rename']
        content = content.replace(
            f'<span class="tag-sm">{old_text}</span>',
            f'<span class="tag-sm">{new_text}</span>'
        )

    # Fix prompt-meta
    if 'prompt_meta_old' in fix:
        content = content.replace(
            f'<p class="prompt-meta">{fix["prompt_meta_old"]}</p>',
            f'<p class="prompt-meta">{fix["prompt_meta_new"]}</p>'
        )

    if content != original:
        with open(path, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f'Updated: {fname}')
    else:
        print(f'No change: {fname}')
