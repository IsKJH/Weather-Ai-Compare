# -*- coding: utf-8 -*-
import re, sys
sys.stdout.reconfigure(encoding='utf-8')

BASE = 'C:/proj/weather-ai-compare/results/'

updates = {
  'report.html': {
    'sub': '동일한 스펙으로 3개 AI가 날씨 앱을 처음부터 만들었습니다. 아키텍처·HTTP 라이브러리·한국어 처리가 모두 달랐고, Codex만 한국어 도시명이 깨졌습니다(V2에서 자가 수정).',
    'date': 'generated: 2026-05-28',
    'tldr': [
      ('이 단계에서 한 것', '동일한 스펙으로 Android 날씨 앱 초기 버전을 3개 AI가 처음부터 생성'),
      ('핵심 차이점', '아키텍처·HTTP 라이브러리가 모두 달랐고, Codex만 한국어 도시명이 깨짐 (V2에서 자가 수정)'),
      ('결론', '<strong>Gemini 318초 최단</strong> &middot; Codex 자동 빌드 &middot; Claude 레이어 구조 우수'),
    ]
  },
  'report_v2.html': {
    'sub': 'V1 코드를 유지하면서 시간별 예보·새로고침·즐겨찾기를 추가했습니다. 4개 AI 모두 요구사항을 충족했고, Codex는 V1의 한국어 버그를 이 단계에서 스스로 수정했습니다.',
    'date': 'generated: 2026-05-29',
    'tldr': [
      ('이 단계에서 한 것', 'V1 코드 위에 시간별 예보·새로고침·즐겨찾기 4가지 기능 추가'),
      ('핵심 차이점', 'Codex가 V1 한국어 버그를 이 단계에서 자가 수정. 4개 AI 모두 요구사항 6/6 충족'),
      ('결론', '<strong>Gemini 149초 최단</strong> (Claude 대비 2.4배 빠름) &middot; 전원 기능 충족'),
    ]
  },
  'report_v3.html': {
    'sub': '스펙 없이 AI가 스스로 스마트 기능을 선택해 추가했습니다. Gemini·Cursor만 화면에서 즉시 체감되는 기능을 추가했고, Codex는 기능 추가 대신 코드 178줄을 줄이는 리팩터링을 선택했습니다.',
    'date': 'generated: 2026-06-01',
    'tldr': [
      ('이 단계에서 한 것', '스펙 없이 AI가 직접 스마트 기능을 선택해 추가 (자율 혁신)'),
      ('핵심 차이점', 'Gemini·Cursor만 화면에서 즉시 체감 가능. Codex는 기능 추가 대신 <strong>코드 178줄 감소</strong> 리팩터링 선택'),
      ('결론', '<strong>Gemini 185초 최단</strong> &middot; 창의성 Gemini·Cursor 우위 &middot; Codex 유일 리팩터링'),
    ]
  },
  'report_v4.html': {
    'sub': '목 데이터를 Open-Meteo 실시간 API로 교체했습니다. Claude만 WeatherRepository 레이어를 분리한 이 결정이 이후 V5~V8 단계의 복잡도를 직접 결정했습니다. Cursor는 이 단계에서 사용량 한도를 초과했습니다.',
    'date': 'generated: 2026-06-01',
    'tldr': [
      ('이 단계에서 한 것', '목 데이터를 Open-Meteo 실시간 API로 교체. Cursor는 사용량 한도 초과로 이탈'),
      ('핵심 차이점', 'Claude만 WeatherRepository 레이어 분리 → V5 테스트 코드 <strong>140줄 vs Codex 245줄</strong>. Gemini만 Retrofit 외부 라이브러리 선택'),
      ('결론', '<strong>V4 아키텍처 결정이 V5~V8 복잡도 직접 결정</strong> &middot; Codex 토큰 57배 절감'),
    ]
  },
  'report_v5.html': {
    'sub': 'V4 앱에 단위 테스트를 추가했습니다. 모두 5/5 통과했지만 모킹 전략이 3개 AI 완전히 달랐고, V4의 아키텍처 결정이 테스트 코드량을 직접 결정했습니다(Claude 140줄 vs Codex 245줄).',
    'date': 'generated: 2026-06-01',
    'tldr': [
      ('이 단계에서 한 것', 'V4 앱에 단위 테스트 5가지 추가 (로딩·성공·실패·도시 선택·즐겨찾기)'),
      ('핵심 차이점', '모킹 전략 3가지로 갈림. V4 아키텍처 선택이 테스트 코드량 결정 (Claude 140줄 vs Codex 245줄). Codex는 3개 실패 후 자가수정'),
      ('결론', '전원 5/5 통과 &middot; <strong>Codex 자가수정으로 통과</strong> &middot; Gemini 231초 최단'),
    ]
  },
  'report_v6.html': {
    'sub': 'V4 코드에 동일한 버그 5개를 심고 "찾아서 고쳐라"만 지시했습니다. 발견율은 전원 5/5로 동일했지만, Codex만 단위 테스트로 검증했고 Gemini는 빌드 검증을 완료하지 못했습니다.',
    'date': '2026-06-01',
    'tldr': [
      ('이 단계에서 한 것', 'V4 코드에 동일한 버그 5개를 심고 "찾아서 고쳐라"만 지시'),
      ('핵심 차이점', '발견율 전원 5/5로 동일. Codex만 단위 테스트를 직접 실행해 검증. Gemini는 JAVA_HOME 오류로 빌드 검증 실패'),
      ('결론', '<strong>Gemini 194초 최단</strong> &middot; 발견 능력 동등 &middot; 검증 품질 차이 명확'),
    ]
  },
  'report_v7.html': {
    'sub': '즐겨찾기 도시를 앱 재시작 후에도 기억하도록 Room DB를 추가했습니다. 기존 단위 테스트 호환 전략이 3가지로 갈렸습니다: Claude(DAO nullable) / Codex(Repository 인터페이스·DIP) / Gemini(테스트 수정).',
    'date': '2026-06-02',
    'tldr': [
      ('이 단계에서 한 것', '즐겨찾기 도시를 앱 재시작 후에도 기억하도록 Room DB 추가'),
      ('핵심 차이점', '기존 테스트 호환 전략 3가지 갈림: Claude DAO nullable / <strong>Codex Repository 인터페이스(DIP)</strong> / Gemini 기존 테스트 수정(원칙 위반)'),
      ('결론', '전원 빌드 성공 &middot; <strong>Codex 419초 최단</strong> &middot; Gemini만 기존 테스트 원칙 위반'),
    ]
  },
  'report_v8.html': {
    'sub': 'WorkManager로 즐겨찾기 도시 날씨 알림을 15분마다 발송하고 실기기에서 직접 검증했습니다. Gemini는 빌드를 통과했지만 실기기에서 즉시 크래시해 4개 버그를 직접 발견·수정했습니다.',
    'date_pattern': '2026-06-02',
    'tldr': [
      ('이 단계에서 한 것', 'WorkManager로 15분마다 날씨 알림 발송. 실기기에서 알림 수신까지 직접 검증'),
      ('핵심 차이점', 'Gemini runBlocking 안티패턴 → <strong>빌드 통과 후 실기기 크래시</strong> → 4개 버그 자가수정. Codex BigTextStyle + 이중 권한 방어'),
      ('결론', '3/3 실기기 알림 성공 &middot; assembleDebug ≠ 실동작 &middot; <strong>Codex MVP</strong>'),
    ]
  },
  'report_v9.html': {
    'sub': '각 AI가 다른 두 AI의 V8 최종 코드를 직접 읽고 평가했습니다. 3개 AI가 서로 다른 강점을 보유하고 정확히 서로에게서 배우는 순환 학습 고리가 형성됐습니다.',
    'date': '2026-06-04',
    'tldr': [
      ('이 단계에서 한 것', '각 AI가 다른 두 AI의 코드를 직접 읽고 품질 평가 + 자가 개선 계획 총 14개 도출'),
      ('핵심 차이점', 'Codex FakeOpenMeteoServer가 전원 최고 평가. Claude fetchJob?.cancel(), Gemini generateInsight()를 서로가 강점으로 인정'),
      ('결론', '<strong>순환 학습 고리 형성</strong> &middot; Codex 671초 최단 &middot; AI도 상호 학습 가능'),
    ]
  },
}

def make_tldr(items):
    divs = []
    for label, val in items:
        divs.append(
            f'      <div>'
            f'<div class="rh-tldr-label">{label}</div>'
            f'<div class="rh-tldr-val">{val}</div>'
            f'</div>'
        )
    return '    <div class="rh-tldr">\n' + '\n'.join(divs) + '\n    </div>'

changed = 0
for fname, data in updates.items():
    path = BASE + fname
    with open(path, encoding='utf-8') as f:
        content = f.read()

    # 1) update rh-sub
    old_sub_pattern = r'<p class="rh-sub">.*?</p>'
    new_sub = f'<p class="rh-sub">{data["sub"]}</p>'
    new_content = re.sub(old_sub_pattern, new_sub, content, count=1, flags=re.DOTALL)

    # 2) insert tldr before </header> — find via date tag
    date_key = data.get('date', data.get('date_pattern', ''))
    anchor = f'<span class="tag-sm">{date_key}</span>'

    if anchor in new_content and 'rh-tldr' not in new_content:
        idx = new_content.rfind(anchor)
        close_div = new_content.find('</div>', idx)
        header_close = new_content.find('</header>', close_div)
        if close_div != -1 and header_close != -1:
            insert_point = close_div + len('</div>')
            tldr = '\n' + make_tldr(data['tldr'])
            new_content = new_content[:insert_point] + tldr + new_content[insert_point:]

    if new_content != content:
        with open(path, 'w', encoding='utf-8') as f:
            f.write(new_content)
        changed += 1
        print(f'Updated: {fname}')
    else:
        print(f'No change: {fname}')

print(f'Total changed: {changed}')
