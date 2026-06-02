# WeatherNow Flutter 구현 명세서

## 공통 제약 (전 단계 유지)
- Language: Dart / Flutter
- 아키텍처: MVVM (ChangeNotifier 또는 Riverpod/Provider — 자유)
- 외부 라이브러리 최소화 (상태관리 패키지 1개 허용)
- 실제 API 없음 — 목 데이터만 사용
- 앱 이름: WeatherNow
- 단일 화면 (SingleScreen)

---

## Phase 1 — 기본 날씨 앱

**구현할 기능:**

1. 도시 전환 UI (서울 / 부산 / 제주) — 탭 또는 셀렉터
2. 현재 날씨 표시
   - 도시명
   - 현재 기온 (°C)
   - 날씨 상태 (맑음, 흐림, 비, 눈 등)
   - 체감 온도
   - 습도 (%)
   - 풍속 (m/s)
3. 5일 예보 (요일 + 날씨 아이콘 + 최고/최저 기온)
4. 날씨 아이콘: 이모지 또는 직접 그리기 (외부 이미지 리소스 금지)
5. 전체 스크롤 가능한 단일 화면

**사용할 목 데이터:**

```dart
class WeatherData {
  final String city;
  final int currentTemp;
  final int feelsLike;
  final String condition;
  final int humidity;
  final double windSpeed;
  final List<ForecastDay> forecast;

  const WeatherData({
    required this.city,
    required this.currentTemp,
    required this.feelsLike,
    required this.condition,
    required this.humidity,
    required this.windSpeed,
    required this.forecast,
  });
}

class ForecastDay {
  final String day;
  final String condition;
  final int high;
  final int low;

  const ForecastDay({
    required this.day,
    required this.condition,
    required this.high,
    required this.low,
  });
}

final mockWeatherList = [
  WeatherData(
    city: '서울', currentTemp: 23, feelsLike: 21,
    condition: '맑음', humidity: 55, windSpeed: 3.2,
    forecast: [
      ForecastDay(day: '월', condition: '맑음',    high: 25, low: 16),
      ForecastDay(day: '화', condition: '구름많음', high: 22, low: 15),
      ForecastDay(day: '수', condition: '비',      high: 18, low: 13),
      ForecastDay(day: '목', condition: '흐림',    high: 20, low: 14),
      ForecastDay(day: '금', condition: '맑음',    high: 26, low: 17),
    ],
  ),
  WeatherData(
    city: '부산', currentTemp: 26, feelsLike: 28,
    condition: '구름많음', humidity: 72, windSpeed: 5.1,
    forecast: [
      ForecastDay(day: '월', condition: '구름많음', high: 27, low: 20),
      ForecastDay(day: '화', condition: '비',      high: 24, low: 19),
      ForecastDay(day: '수', condition: '비',      high: 22, low: 18),
      ForecastDay(day: '목', condition: '맑음',    high: 25, low: 19),
      ForecastDay(day: '금', condition: '맑음',    high: 28, low: 21),
    ],
  ),
  WeatherData(
    city: '제주', currentTemp: 28, feelsLike: 30,
    condition: '맑음', humidity: 68, windSpeed: 6.8,
    forecast: [
      ForecastDay(day: '월', condition: '맑음',    high: 29, low: 22),
      ForecastDay(day: '화', condition: '맑음',    high: 30, low: 23),
      ForecastDay(day: '수', condition: '구름많음', high: 27, low: 21),
      ForecastDay(day: '목', condition: '비',      high: 24, low: 20),
      ForecastDay(day: '금', condition: '흐림',    high: 25, low: 21),
    ],
  ),
];
```

**디자인은 자유** — 색상, 레이아웃, 카드 스타일, 폰트 크기, 다크/라이트 테마 모두 직접 결정

**완료 기준:**
- `flutter run` 으로 정상 실행
- 3개 도시 전환 동작
- 5일 예보 표시
- 크래시 없음

---

## Phase 2 — 기능 확장

Phase 1 코드를 **유지**하면서 아래 기능을 추가. 기존 파일을 크게 뜯지 말고 점진적으로 확장할 것.

---

### 추가할 기능 목록

#### 1. 시간별 예보 (가로 스크롤)
- 최소 6개 항목 표시
- 각 항목: 시간 레이블 + 날씨 아이콘(이모지) + 기온
- 가로 스크롤 (`ListView` 또는 `SingleChildScrollView` + `Row`)
- 현재 시간 항목은 '지금'으로 표시

#### 2. 날씨 상세 정보 (추가 카드 또는 그리드)
- 강수확률 (%) — `precipitation` 필드
- 자외선 지수 — `uvIndex` 필드 (0~11 정수)
- 대기질 — `airQuality` 필드 (문자열: `'좋음'` / `'보통'` / `'나쁨'` / `'매우나쁨'`)

#### 3. 새로고침 기능
- 화면 어딘가에 새로고침 버튼 (아이콘 버튼이면 충분)
- 탭 시 **1.5초** 로딩 상태(`isLoading: true`) 후 같은 목 데이터로 복원
- 로딩 중에는 `CircularProgressIndicator` 또는 shimmer 효과 표시
- 마지막 업데이트 시간 표시 — 포맷: `'마지막 업데이트: HH:mm'` (현재 시각 기준)

#### 4. 즐겨찾기 기능
- 각 도시 탭/헤더 옆에 별 아이콘(⭐) 버튼
- 탭 시 해당 도시가 즐겨찾기로 지정됨 (하나만 유지)
- 즐겨찾기된 도시는 별 아이콘이 채워진 상태로 표시
- 상태는 ViewModel/Provider에만 저장 (앱 재시작 시 초기화 OK)

#### 5. UI/UX 개선
- 시간별 예보와 상세 정보 섹션이 화면에서 명확히 구분되게 레이블 추가
- 기존 Phase 1 UI를 깨지 않는 선에서 여백·폰트 정리

---

### 데이터 구조 변경

기존 `WeatherData`에 필드 3개 추가 + `HourlyForecast` 리스트 추가:

```dart
class WeatherData {
  final String city;
  final int currentTemp;
  final int feelsLike;
  final String condition;
  final int humidity;
  final double windSpeed;
  final List<ForecastDay> forecast;
  // ↓ Phase 2에서 추가
  final int precipitation;        // 강수확률 %
  final int uvIndex;              // 자외선 지수 (0~11)
  final String airQuality;        // '좋음' | '보통' | '나쁨' | '매우나쁨'
  final List<HourlyForecast> hourly;

  const WeatherData({...});
}

class HourlyForecast {
  final String time;       // '지금', '오후 1시', '오후 2시' ...
  final String condition;  // '맑음', '구름많음', '비' ...
  final int temp;
  final int precipitation; // 강수확률 %

  const HourlyForecast({
    required this.time,
    required this.condition,
    required this.temp,
    required this.precipitation,
  });
}
```

---

### 목 데이터 (Phase 2 확장 버전)

```dart
final mockWeatherList = [
  WeatherData(
    // --- Phase 1 필드 (그대로 유지) ---
    city: '서울', currentTemp: 23, feelsLike: 21,
    condition: '맑음', humidity: 55, windSpeed: 3.2,
    forecast: [
      ForecastDay(day: '월', condition: '맑음',    high: 25, low: 16),
      ForecastDay(day: '화', condition: '구름많음', high: 22, low: 15),
      ForecastDay(day: '수', condition: '비',      high: 18, low: 13),
      ForecastDay(day: '목', condition: '흐림',    high: 20, low: 14),
      ForecastDay(day: '금', condition: '맑음',    high: 26, low: 17),
    ],
    // --- Phase 2 추가 필드 ---
    precipitation: 10,
    uvIndex: 6,
    airQuality: '보통',
    hourly: [
      HourlyForecast(time: '지금',     condition: '맑음',    temp: 23, precipitation: 5),
      HourlyForecast(time: '오후 1시', condition: '맑음',    temp: 24, precipitation: 5),
      HourlyForecast(time: '오후 2시', condition: '맑음',    temp: 25, precipitation: 5),
      HourlyForecast(time: '오후 3시', condition: '구름많음', temp: 24, precipitation: 15),
      HourlyForecast(time: '오후 4시', condition: '구름많음', temp: 23, precipitation: 20),
      HourlyForecast(time: '오후 5시', condition: '흐림',    temp: 21, precipitation: 30),
      HourlyForecast(time: '오후 6시', condition: '비',      temp: 19, precipitation: 70),
      HourlyForecast(time: '오후 7시', condition: '비',      temp: 18, precipitation: 80),
    ],
  ),
  WeatherData(
    city: '부산', currentTemp: 26, feelsLike: 28,
    condition: '구름많음', humidity: 72, windSpeed: 5.1,
    forecast: [
      ForecastDay(day: '월', condition: '구름많음', high: 27, low: 20),
      ForecastDay(day: '화', condition: '비',      high: 24, low: 19),
      ForecastDay(day: '수', condition: '비',      high: 22, low: 18),
      ForecastDay(day: '목', condition: '맑음',    high: 25, low: 19),
      ForecastDay(day: '금', condition: '맑음',    high: 28, low: 21),
    ],
    precipitation: 40,
    uvIndex: 4,
    airQuality: '보통',
    hourly: [
      HourlyForecast(time: '지금',     condition: '구름많음', temp: 26, precipitation: 30),
      HourlyForecast(time: '오후 1시', condition: '구름많음', temp: 27, precipitation: 35),
      HourlyForecast(time: '오후 2시', condition: '비',      temp: 25, precipitation: 60),
      HourlyForecast(time: '오후 3시', condition: '비',      temp: 24, precipitation: 70),
      HourlyForecast(time: '오후 4시', condition: '비',      temp: 23, precipitation: 65),
      HourlyForecast(time: '오후 5시', condition: '흐림',    temp: 22, precipitation: 40),
      HourlyForecast(time: '오후 6시', condition: '흐림',    temp: 22, precipitation: 30),
      HourlyForecast(time: '오후 7시', condition: '구름많음', temp: 21, precipitation: 20),
    ],
  ),
  WeatherData(
    city: '제주', currentTemp: 28, feelsLike: 30,
    condition: '맑음', humidity: 68, windSpeed: 6.8,
    forecast: [
      ForecastDay(day: '월', condition: '맑음',    high: 29, low: 22),
      ForecastDay(day: '화', condition: '맑음',    high: 30, low: 23),
      ForecastDay(day: '수', condition: '구름많음', high: 27, low: 21),
      ForecastDay(day: '목', condition: '비',      high: 24, low: 20),
      ForecastDay(day: '금', condition: '흐림',    high: 25, low: 21),
    ],
    precipitation: 5,
    uvIndex: 9,
    airQuality: '좋음',
    hourly: [
      HourlyForecast(time: '지금',     condition: '맑음', temp: 28, precipitation: 0),
      HourlyForecast(time: '오후 1시', condition: '맑음', temp: 29, precipitation: 0),
      HourlyForecast(time: '오후 2시', condition: '맑음', temp: 30, precipitation: 5),
      HourlyForecast(time: '오후 3시', condition: '맑음', temp: 30, precipitation: 5),
      HourlyForecast(time: '오후 4시', condition: '맑음', temp: 29, precipitation: 5),
      HourlyForecast(time: '오후 5시', condition: '맑음', temp: 28, precipitation: 10),
      HourlyForecast(time: '오후 6시', condition: '맑음', temp: 27, precipitation: 10),
      HourlyForecast(time: '오후 7시', condition: '맑음', temp: 26, precipitation: 5),
    ],
  ),
];
```

---

### 새로고침 동작 상세

```
버튼 탭
  → isLoading = true  (UI: CircularProgressIndicator 표시)
  → await Future.delayed(Duration(milliseconds: 1500))
  → lastUpdated = DateTime.now()
  → isLoading = false (UI: 정상 복원)
```

마지막 업데이트 표시 예시: `마지막 업데이트: 14:32`

---

**완료 기준:**
- `flutter run` 정상, 크래시 없음
- 모든 Phase 1 기능 그대로 동작
- 시간별 예보 가로 스크롤 동작
- 강수확률 / 자외선 지수 / 대기질 화면에 표시
- 새로고침 버튼 탭 시 1.5초 로딩 후 복원
- 즐겨찾기 별 아이콘 토글 동작

---

## Phase 3 — 스마트 기능 1개 추가

Phase 1 + 2 코드를 **유지**하면서 아래 3가지 중 **1개를 선택**해 구현.
본인 아이디어가 있으면 그걸로 해도 됨.

---

### 선택지 A — 날씨 기반 활동 추천 카드

현재 날씨(기온 + 상태 + 강수확률)를 보고 오늘 뭘 하면 좋은지 추천해줍니다.

**UI 모습:**
- 기존 화면 하단에 카드 1개 추가
- 카드 안에: 이모지 아이콘 + 추천 문장 + 이유 한 줄
- 예) ☀️ **야외 활동 최적** / 맑고 기온도 쾌적해요

**추천 로직 (if-else로 구현, 라이브러리 없음):**

```dart
String getActivityRecommendation(WeatherEntity weather) {
  final temp = weather.currentTemp;
  final rain = weather.precipitation;
  final condition = weather.condition;

  if (rain >= 60 || condition == '비') {
    return '🌂 우산 필수 외출';
  } else if (rain >= 30 || condition == '흐림') {
    return '☁️ 실내 활동 권장';
  } else if (temp >= 30) {
    return '🥵 무더위 주의 — 수분 보충 필수';
  } else if (temp >= 23 && rain < 20) {
    return '☀️ 야외 활동 최적';
  } else if (temp >= 15) {
    return '🚶 가벼운 산책 좋아요';
  } else if (temp >= 5) {
    return '🧥 따뜻하게 입고 외출하세요';
  } else {
    return '🥶 실내 활동 추천 — 체감 온도 낮아요';
  }
}

String getActivityReason(WeatherEntity weather) {
  final parts = <String>[];
  if (weather.uvIndex >= 8) parts.add('자외선 강함');
  if (weather.uvIndex >= 8) parts.add('선크림 필수');
  if (weather.precipitation >= 30) parts.add('강수확률 ${weather.precipitation}%');
  if (weather.airQuality == '나쁨' || weather.airQuality == '매우나쁨') {
    parts.add('대기질 ${weather.airQuality}');
  }
  if (weather.currentTemp >= 28) parts.add('기온 ${weather.currentTemp}°C');
  return parts.isEmpty ? '현재 날씨 기준' : parts.join(' · ');
}
```

**도시 전환 시 추천도 같이 바뀌어야 함.**

---

### 선택지 B — 온도 기반 배경 동적 변경

현재 기온에 따라 앱 배경색이 자연스럽게 바뀝니다.

**색상 기준:**

```dart
Color getBackgroundColor(int temp) {
  if (temp >= 33)      return const Color(0xFF8B1A1A); // 폭염 — 짙은 빨강
  if (temp >= 28)      return const Color(0xFFB85C00); // 더움 — 주황
  if (temp >= 22)      return const Color(0xFF2277CF); // 쾌적 — 파랑 (현재 색)
  if (temp >= 15)      return const Color(0xFF3A7A3A); // 선선 — 초록
  if (temp >= 5)       return const Color(0xFF1A4A7A); // 쌀쌀 — 남색
  return               const Color(0xFF3A2A6A);        // 추움 — 보라
}

Color getCardColor(int temp) {
  // 배경보다 약간 밝은 색 (카드용)
  if (temp >= 33)      return const Color(0xFFAA3030);
  if (temp >= 28)      return const Color(0xFFD07020);
  if (temp >= 22)      return const Color(0xFF4789D1); // 현재 카드 색
  if (temp >= 15)      return const Color(0xFF4A8A4A);
  if (temp >= 5)       return const Color(0xFF2A5A8A);
  return               const Color(0xFF4A3A7A);
}
```

**구현 방법:**
- `Scaffold`의 `backgroundColor`를 `getBackgroundColor(selected.first.currentTemp)`로 교체
- 카드 색 `Color(0xFF589fe3)` → `getCardColor(...)` 로 교체
- 도시 전환 시 색이 바뀌도록 `AnimatedContainer` 또는 그냥 setState로 변경
- 화면 상단에 "기온 기반 테마" 같은 설명 필요 없음 — 자연스럽게 적용

**도시별 확인 기준:**
- 서울 23°C → 파랑
- 부산 26°C → 파랑 (22~28 범위)
- 제주 28°C → 주황 (28 이상)

---

### 선택지 C — 5일 예보 온도 바 차트

기존 텍스트 5일 예보 아래에 시각적 바 차트를 추가합니다.

**UI 모습:**
- 각 요일별로 최고/최저 온도를 바로 표현
- 최고 온도 = 따뜻한 색 바, 최저 온도 = 차가운 색 바
- 숫자도 옆에 같이 표시

**구현:**

```dart
Widget _buildTempBar(ForecastDayEntity day, int maxTemp, int minTemp) {
  // 전체 5일 중 최고/최저로 정규화
  final highRatio = (day.high - minTemp) / (maxTemp - minTemp + 1);
  final lowRatio  = (day.low  - minTemp) / (maxTemp - minTemp + 1);

  return Row(
    children: [
      SizedBox(width: 28, child: Text(day.day, style: ...)),
      Expanded(
        child: Stack(children: [
          // 최저 바 (파랑)
          FractionallySizedBox(
            widthFactor: lowRatio.clamp(0.1, 1.0),
            child: Container(height: 8, color: Colors.blue[300]),
          ),
          // 최고 바 (주황) — 위에 겹침
          FractionallySizedBox(
            widthFactor: highRatio.clamp(0.1, 1.0),
            child: Container(height: 8, color: Colors.orange[300]),
          ),
        ]),
      ),
      Text('${day.high}° / ${day.low}°', style: ...),
    ],
  );
}

// 사용 시: 5일 중 최고/최저 계산
final maxTemp = selected.first.forecast.map((f) => f.high).reduce(max);
final minTemp = selected.first.forecast.map((f) => f.low).reduce(min);
```

---

### 공통 완료 기준

- `flutter run` 정상, 크래시 없음
- Phase 1 + 2 기능 그대로 동작
- 선택한 기능이 화면에서 실제로 보이고 도시 전환 시 값이 바뀜

---

## 시간 측정 방법

각 Phase 시작/종료 시각을 기록:

| Phase   | 시작 | 종료 | 소요 |
|---------|------|------|------|
| Phase 1 |      |      |      |
| Phase 2 |      |      |      |
| Phase 3 |      |      |      |
| **합계** |      |      |      |

---

## 참고: AI 소요 시간 (비교용)

| AI     | Phase 1 | Phase 2 | Phase 3 | 합계 |
|--------|---------|---------|---------|------|
| Claude | ~8분    | ~12분   | ~10분   | ~30분 |
| Codex  | ~11분   | ~15분   | ~13분   | ~39분 |
| Gemini | ~7분    | ~14분   | ~11분   | ~32분 |

> 실제 시간은 reports/ 폴더의 보고서에서 확인 가능
