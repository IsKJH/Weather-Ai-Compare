# Weather App - AI Comparison Project

## 프로젝트 목적
3개의 AI(Claude, Codex, Gemini)가 동일한 명세서를 보고 각자 Android 날씨 앱을 만든다.
UI 디자인, 코드 스타일, 구현 방식의 차이를 비교하기 위한 프로젝트다.

---

## 기술 스택 (고정 — 변경 불가)

| 항목 | 값 |
|------|----|
| 언어 | Kotlin |
| UI | Jetpack Compose |
| 아키텍처 | MVVM (ViewModel + UiState) |
| minSdk | 26 |
| targetSdk | 35 |
| 외부 라이브러리 | 없음 (표준 Compose + ViewModel만 사용) |
| 실제 API | 사용 금지 — Mock 데이터만 사용 |

---

## 구현할 기능 (필수)

### 화면 구성
- 메인 화면 1개 (단일 Activity + Compose)
- 앱 이름: **WeatherNow**

### 표시할 정보
1. 도시 이름
2. 현재 기온 (°C)
3. 날씨 상태 (맑음, 흐림, 비, 눈 등)
4. 체감 기온
5. 습도 (%)
6. 풍속 (m/s)
7. 5일 예보 (요일 + 날씨 아이콘 + 최고/최저 기온)

### 기능
- 도시 전환: 서울, 부산, 제주 3개 도시 탭 또는 선택 UI
- 날씨 아이콘: 이모지 또는 Compose로 직접 그린 아이콘 사용 (외부 이미지 리소스 금지)

---

## Mock 데이터 (고정 — 3개 AI 동일)

```kotlin
// 이 데이터를 그대로 사용할 것
data class WeatherData(
    val city: String,
    val currentTemp: Int,
    val feelsLike: Int,
    val condition: String,
    val humidity: Int,
    val windSpeed: Double,
    val forecast: List<ForecastDay>
)

data class ForecastDay(
    val day: String,
    val condition: String,
    val high: Int,
    val low: Int
)

val mockWeatherList = listOf(
    WeatherData(
        city = "서울",
        currentTemp = 23,
        feelsLike = 21,
        condition = "맑음",
        humidity = 55,
        windSpeed = 3.2,
        forecast = listOf(
            ForecastDay("월", "맑음", 25, 16),
            ForecastDay("화", "구름많음", 22, 15),
            ForecastDay("수", "비", 18, 13),
            ForecastDay("목", "흐림", 20, 14),
            ForecastDay("금", "맑음", 26, 17)
        )
    ),
    WeatherData(
        city = "부산",
        currentTemp = 26,
        feelsLike = 28,
        condition = "구름많음",
        humidity = 72,
        windSpeed = 5.1,
        forecast = listOf(
            ForecastDay("월", "구름많음", 27, 20),
            ForecastDay("화", "비", 24, 19),
            ForecastDay("수", "비", 22, 18),
            ForecastDay("목", "맑음", 25, 19),
            ForecastDay("금", "맑음", 28, 21)
        )
    ),
    WeatherData(
        city = "제주",
        currentTemp = 28,
        feelsLike = 30,
        condition = "맑음",
        humidity = 68,
        windSpeed = 6.8,
        forecast = listOf(
            ForecastDay("월", "맑음", 29, 22),
            ForecastDay("화", "맑음", 30, 23),
            ForecastDay("수", "구름많음", 27, 21),
            ForecastDay("목", "비", 24, 20),
            ForecastDay("금", "흐림", 25, 21)
        )
    )
)
```

---

## UI 요구사항

### 자유롭게 결정해도 되는 것 (AI 개성 발휘 영역)
- 색상 테마 및 배경
- 레이아웃 구성 방식
- 날씨 아이콘 표현 방식
- 애니메이션 유무
- 카드/섹션 디자인
- 폰트 크기 및 계층 구조

### 반드시 지켜야 할 것
- 모든 정보가 하나의 스크롤 가능한 화면에 표시될 것
- 도시 전환 UI가 반드시 포함될 것
- 5일 예보가 가로 또는 세로 리스트로 표시될 것
- 다크 또는 라이트 테마 중 하나를 선택하여 일관되게 적용할 것

---

## 프로젝트 구조 (권장)

```
app/
└── src/main/
    ├── java/.../
    │   ├── MainActivity.kt
    │   ├── WeatherViewModel.kt
    │   ├── WeatherData.kt        ← Mock 데이터 포함
    │   └── ui/
    │       ├── WeatherScreen.kt
    │       └── components/       ← 재사용 컴포저블
    └── res/
        └── values/
            ├── strings.xml
            └── themes.xml
```

---

## 완료 기준

- [ ] `./gradlew assembleDebug` 빌드 성공
- [ ] 실기기(ADB)에서 앱 실행 가능
- [ ] 3개 도시 전환 동작
- [ ] 5일 예보 표시
- [ ] 크래시 없음

---

## 참고 — 비교 측정 항목

이 프로젝트는 다음 항목을 비교하기 위해 만들어진다:
- 소요 토큰 수 (입력/출력)
- 생성 소요 시간
- 생성된 코드 라인 수 / 파일 수
- UI 디자인 차이
- 빌드 성공 여부
- 코드 구조 및 스타일 차이
