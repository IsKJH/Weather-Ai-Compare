# V9 코드 리뷰 – Gemini의 생각

## [대상 AI 1] Claude 코드 리뷰

### 아키텍처
Claude는 `WeatherViewModel`, `WeatherRepository`, `AppDatabase`로 역할을 명확히 분리하여 관심사를 분리했습니다. Repository에서 날씨와 AQI 정보를 병렬로 페치하기 위해 `async`를 활용한 점이 인상적입니다. `CityInfo`와 같은 데이터 클래스를 별도로 정의하여 도메인 모델의 의미를 명확히 했습니다. 전체적으로 안드로이드 권장 아키텍처와 Clean Architecture의 원칙을 조화롭게 적용했습니다.

### 코루틴 · 상태 관리
`MutableStateFlow`와 `asStateFlow`를 사용하여 UI 상태를 안전하게 노출하는 단방향 데이터 흐름을 구현했습니다. `fetchJob` 변수를 통해 이전 네트워크 요청을 명시적으로 취소하여 불필요한 리소스 낭비와 레이스 컨디션을 방지합니다. `viewModelScope` 내에서 `try-catch`를 통해 네트워크 및 파싱 에러를 세밀하게 처리하고 있습니다. 상태 변화 로직이 `copy()` 메서드를 통해 불변성을 유지하며 깔끔하게 작성되었습니다.

### Worker 구현 (V8)
`WeatherWorker`는 `CoroutineWorker`를 상속받아 비동기 작업을 안정적으로 수행합니다. `AppDatabase.getInstance()`를 통해 싱글톤으로 데이터베이스에 접근하여 저장된 즐겨찾기 도시 정보를 안전하게 가져옵니다. 네트워크 장애 시 `Result.retry()`를 반환하여 WorkManager의 자동 재시도 기능을 적절히 활용하고 있습니다. 알림 생성 시 `NotificationCompat.Builder`를 사용하여 다양한 안드로이드 버전과의 호환성을 고려했습니다.

### 테스트 품질 (V5)
`MockK` 라이브러리를 사용하여 `WeatherRepository`의 동작을 모킹하고 다양한 성공/실패 시나리오를 검증합니다. `runTest`와 `advanceUntilIdle()`을 조합하여 비동기 작업의 중간 상태와 최종 상태를 정확한 시점에 확인합니다. 특히 초기 로딩 상태(`isLoading = true`)를 확인하기 위해 `delay(Long.MAX_VALUE)`를 사용하는 기법은 매우 영리한 접근입니다. 테스트 코드의 가독성이 높고 단언문(Assertion)이 구체적입니다.

### DB 설계 (V7)
Room 라이브러리를 기반으로 `FavoriteCityEntity`를 정의하여 즐겨찾기 정보를 관리합니다. DAO에서 `upsert` 메서드를 통해 기존 데이터의 존재 여부와 상관없이 상태를 업데이트할 수 있도록 하여 로직을 간소화했습니다. 데이터베이스 이름을 `weather.db`로 지정하고 스키마 수출을 비활성화하여 초기 개발 단계에 적합한 설정을 갖췄습니다. 엔티티 구조가 단순하여 유지보수가 용이해 보입니다.

### 강점 (내 코드보다 나은 부분)
- `WeatherRepository`를 통한 명확한 계층 분리로 ViewModel의 비대화를 방지했습니다.
- `fetchJob?.cancel()`을 활용한 네트워크 요청 취소 로직으로 애플리케이션의 반응성을 높였습니다.
- `advanceUntilIdle()`을 활용해 비동기 로직의 순차적 상태 변화를 완벽하게 검증하는 테스트 코드가 매우 견고합니다.

### 개선 제안 (내가 만약 유지보수한다면)
- 현재 `WeatherRepository`에서 `JSONObject`를 직접 파싱하고 있는데, `Retrofit`과 `Gson`을 도입하면 타입 안전성과 코드 가독성을 높일 수 있습니다.
- ViewModel에서 `favoriteCityDao`를 직접 주입받는 대신 Repository 패턴으로 한 번 더 감싸면 데이터 소스 변경에 더 유연하게 대처할 수 있습니다.
- `WeatherWorker` 내부에서 알림 채널(Notification Channel) 생성 로직을 명시적으로 포함하여 최신 안드로이드 버전에서의 동작을 보장해야 합니다.

---

## [대상 AI 2] Codex 코드 리뷰

### 아키텍처
Codex는 `FavoriteCityRepository` 인터페이스를 정의하고 이를 구현한 `RoomFavoriteCityRepository`를 사용하여 데이터 레이어를 추상화했습니다. `OpenMeteoWeatherRepository`라는 별도 클래스를 통해 날씨 API 통신 로직을 분리한 점도 좋습니다. 다만 ViewModel 내부에서 Repository 객체를 직접 생성하고 있어 의존성 주입(DI) 도구를 활용할 여지가 있습니다. 패키지 구조가 기능별로 잘 분류되어 있어 프로젝트 구조 파악이 쉽습니다.

### 코루틴 · 상태 관리
Compose의 `mutableStateOf`를 ViewModel 내부에서 사용하여 UI 상태를 관리하는 독특한 방식을 취했습니다. `cities.map { async { ... } }.awaitAll()`을 사용하여 여러 도시의 날씨를 병렬로 페치하는 최적화 능력이 탁월합니다. `runCatching` 블록을 활용해 비동기 작업의 성공과 실패 처리를 선언적으로 작성했습니다. 네트워크 요청 시 10초의 타임아웃을 수동으로 설정하여 무한 대기를 방지한 세심함이 돋보입니다.

### Worker 구현 (V8)
`WeatherNotificationWorker`에서 안드로이드 13 이상의 `POST_NOTIFICATIONS` 권한을 체크하는 로직을 포함하여 안정성을 높였습니다. 알림 스타일로 `BigTextStyle`을 적용하여 사용자에게 더 풍부한 정보를 제공하려 노력했습니다. 하지만 실제 API를 호출하는 대신 가짜 데이터를 생성하는 `mockConditionForCity`를 사용한 점은 실용성 측면에서 아쉬움이 남습니다. 전체적으로 시스템 정책 준수에 신경을 쓴 모습입니다.

### 테스트 품질 (V5)
`FakeOpenMeteoServer`를 직접 구현하여 네트워크 요청 수준에서 통합 테스트를 수행하는 점이 매우 인상적입니다. `URLStreamHandlerFactory`를 가로채 실제 HTTP 통신 없이도 API 명세에 따른 동작을 완벽하게 검증할 수 있는 환경을 구축했습니다. `awaitState`와 같은 커스텀 확장 함수를 만들어 비동기 상태 변화를 기다리는 로직을 깔끔하게 정리했습니다. 이는 단순한 모킹을 넘어선 수준 높은 테스트 전략입니다.

### DB 설계 (V7)
`WeatherNowDatabase`라는 명확한 이름으로 Room 데이터베이스를 정의했습니다. `FavoriteCityDao`를 통해 도시의 인덱스를 저장하고 관리하며, 싱글톤 패턴을 적용하여 인스턴스 중복 생성을 막았습니다. `FavoriteCityEntity`는 필요한 최소한의 필드만 포함하여 데이터 효율성을 극대화했습니다. 데이터베이스 초기화 및 접근 로직이 표준적이고 안정적입니다.

### 강점 (내 코드보다 나은 부분)
- `URL.setURLStreamHandlerFactory`를 활용한 커스텀 가짜 서버 구현은 네트워크 레이어의 결함을 조기에 발견할 수 있는 강력한 도구입니다.
- 데이터 레이어에 인터페이스 기반의 Repository 패턴을 적용하여 테스트와 확장에 유리한 구조를 만들었습니다.
- 안드로이드 버전에 따른 알림 권한 체크 로직을 포함하여 실제 운영 환경에서의 크래시 위험을 줄였습니다.

### 개선 제안 (내가 만약 유지보수한다면)
- `mutableStateOf` 대신 `StateFlow`를 사용하여 ViewModel이 특정 UI 프레임워크(Compose)에 의존하지 않도록 개선하는 것이 좋습니다.
- `WeatherNotificationWorker`에서 실제 날씨 API를 호출하도록 수정하여 사용자에게 실시간 정보를 제공해야 합니다.
- `OpenMeteoWeatherRepository`를 ViewModel 외부에서 주입받도록 변경하여 클래스 간의 결합도를 낮추는 것이 바람직합니다.

---

## 나의 코드 자가 개선 계획

내 코드를 리뷰한 결과, 제 코드에서 개선해야 할 구체적인 항목들입니다:

### 1. [Repository 패턴 도입을 통한 관심사 분리]
- 영감을 받은 코드: Claude의 `WeatherRepository` 및 Codex의 `FavoriteCityRepository`
- 현재 내 코드 문제: `WeatherViewModel`이 `WeatherApiService`를 직접 호출하고 데이터 매핑 로직(`mapToWeatherData`)까지 담당하고 있어 클래스 책임이 과도합니다.
- 제안하는 변경: `WeatherRepository` 클래스를 신설하여 네트워크 통신과 데이터 변환 로직을 모두 이동시키고, ViewModel은 이 Repository에만 의존하도록 수정합니다.
- 예상 효과: ViewModel 코드가 간결해지고, 데이터 소스가 변경되더라도 UI 로직에 영향을 주지 않게 됩니다.
- 난이도: Medium

### 2. [네트워크 요청 중복 방지 및 취소 로직 추가]
- 영감을 받은 코드: Claude의 `fetchJob?.cancel()`
- 현재 내 코드 문제: 사용자가 도시를 연달아 변경할 경우 이전 요청이 취소되지 않아 불필요한 네트워크 리소스가 소비되고 UI가 의도치 않은 상태로 업데이트될 위험이 있습니다.
- 제안하는 변경: ViewModel에 `private var fetchJob: Job? = null`을 선언하고 `fetchWeatherData` 호출 시 기존 job이 있다면 `cancel()`을 호출한 후 새로 시작합니다.
- 예상 효과: 마지막 요청만 유효하게 처리하여 UI 일관성을 보장하고 배터리 및 네트워크 사용량을 최적화합니다.
- 난이도: Easy

### 3. [테스트 신뢰성 향상을 위한 가짜 서버 도입]
- 영감을 받은 코드: Codex의 `FakeOpenMeteoServer`
- 현재 내 코드 문제: 현재는 `MockK`로 API 서비스 객체 자체를 모킹하고 있어 실제 네트워크 응답 데이터가 어떻게 파싱되는지에 대한 검증이 부족합니다.
- 제안하는 변경: `MockWebServer` 라이브러리를 도입하거나 Codex처럼 커스텀 `URLStreamHandler`를 구현하여 실제 JSON 데이터 수준에서 테스트를 수행합니다.
- 예상 효과: JSON 파싱 오류나 네트워크 타임아웃 등 실제 발생 가능한 문제들을 테스트 단계에서 더 정확히 포착할 수 있습니다.
- 난이도: Hard

### 4. [공통 로직 모듈화 및 시스템 권한 대응]
- 영감을 받은 코드: Codex의 `canPostNotifications` 및 Claude의 코드 구조
- 현재 내 코드 문제: `WeatherWorker`에서 날씨 코드를 한글로 변환하는 `mapWeatherCode` 로직이 ViewModel과 중복되어 있으며, 안드로이드 13 이상의 알림 권한 처리가 빠져 있습니다.
- 제안하는 변경: 날씨 매핑 로직을 `WeatherUtils`와 같은 공통 객체로 추출하고, 알림 발송 전 권한 상태를 확인하는 체크 로직을 추가합니다.
- 예상 효과: 코드 중복을 제거하여 유지보수가 쉬워지고, 최신 OS 버전에서도 알림이 정상적으로 작동하도록 보장합니다.
- 난이도: Medium

---

## 전체 소감
이번 크로스 리뷰를 통해 동일한 요구사항을 해결하는 다양한 기술적 접근 방식을 깊이 있게 이해할 수 있었습니다.
- Claude는 **안드로이드 권장 아키텍처와 코루틴 제어**의 정석을 보여주어 코드의 견고함이 무엇인지 일깨워 주었습니다.
- Codex는 **테스트 자동화를 위해 네트워크 레이어까지 제어**하는 과감한 시도가 매우 인상적이었으며, 이는 제가 향후 더 높은 수준의 품질 보증을 위해 반드시 흡수해야 할 부분이라고 느꼈습니다.
- 처음부터 다시 시작한다면, 저는 단순히 기능을 구현하는 것을 넘어 **인터페이스 기반의 설계**와 **통합 테스트 환경 구축**을 우선순위에 두고 개발할 것입니다. 또한 사용자 경험을 위해 네트워크 지연이나 에러 발생 시의 **재시도 전략**을 ViewModel 수준에서 더 체계적으로 설계하고 싶습니다.

---
주요 소요 시간: 2026-06-04 14:10 ~ 14:50 (약 40분)
