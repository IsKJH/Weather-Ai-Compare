# V9 코드 리뷰 — Gemini의 시각

## Claude 코드 리뷰

### 아키텍처
패키지 구조가 `viewmodel`, `data`, `data.db`, `worker`로 명확하게 분리되어 있어 유지보수성이 높습니다. `WeatherRepository`를 싱글톤 객체(object)로 정의하여 전역적으로 접근 가능하게 했으며, API 호출과 데이터 파싱 로직을 ViewModel로부터 잘 분리해냈습니다. 다만, Repository가 인터페이스가 아닌 객체로 구현되어 있어 테스트 시 `mockkObject`를 사용해야 하는 제약이 있습니다.

### 코루틴 · 상태 관리
`WeatherViewModel`에서 `StateFlow`를 사용하여 UI 상태를 관리하는 정석적인 방법을 보여줍니다. 특히 `fetchJob: Job?` 변수를 활용하여 새로운 날씨 요청이 들어올 때 이전 요청을 `cancel()`하는 로직은 네트워크 리소스 낭비와 레이스 컨디션을 방지하는 아주 좋은 실무적 접근입니다. `LocalTime`을 활용한 `lastUpdated` 갱신 로직도 깔끔합니다.

### Worker 구현 (V8)
`WeatherWorker`에서 `CoroutineWorker`를 상속받아 구현하였으며, DB에서 즐겨찾기 정보를 가져와 실제 `WeatherRepository`를 통해 날씨를 조회하는 흐름이 매끄럽습니다. 네트워크 예외 발생 시 `Result.retry()`를 반환하여 WorkManager의 자동 재시도 기능을 활용한 점이 훌륭합니다. 알림 채널 ID와 알림 ID를 `companion object`로 관리하여 코드 가독성을 높였습니다.

### 테스트 품질 (V5)
`MockK` 라이브러리를 적극적으로 활용하여 `WeatherRepository`의 동작을 모킹했습니다. `StandardTestDispatcher`와 `advanceUntilIdle()`을 사용하여 비동기 상태 변화를 정밀하게 검증했습니다. 특히 로딩 상태를 테스트하기 위해 `delay(Long.MAX_VALUE)`를 사용하는 기법은 로딩 UI 검증의 정석을 보여줍니다.

### DB 설계 (V7)
Room을 사용하여 `FavoriteCityEntity`를 정의하고, `upsert()`(Update + Insert) 기능을 활용해 단일 즐겨찾기 정보를 효율적으로 관리합니다. `AppDatabase`는 전형적인 싱글톤 패턴을 따르고 있으며, `exportSchema = false` 설정을 통해 불필요한 스키마 내보내기를 방지했습니다.

### 잘 한 점 (내 코드보다 나은 부분)
- **`WeatherViewModel.fetchJob`을 통한 코루틴 취소 제어**: 새로운 요청 시 이전 요청을 명시적으로 취소하여 데이터 일관성을 보장함.
- **`WeatherRepository`의 철저한 관심사 분리**: JSON 파싱 로직과 URL 생성 로직이 Repository 내부에 캡슐화되어 있어 ViewModel이 매우 가볍습니다.
- **날씨 코드(WMO) 매핑 로직의 세분화**: `wmoToCondition` 메서드를 통해 다양한 날씨 코드를 한글 문자열로 상세히 변환함.

### 개선 제안 (내가 유지보수한다면 바꿀 것)
- **Repository 인터페이스 도입**: 현재 `object WeatherRepository` 대신 인터페이스를 정의하고 이를 주입받는 방식을 사용하면, 테스트 시 `mockkObject` 대신 가짜(Fake) 객체를 사용할 수 있어 더 유연할 것 같습니다.
- **UI State의 세분화**: `WeatherUiState` 하나에 모든 정보가 들어있는데, 에러 처리를 위한 `Event` 래퍼나 별도의 `Effect` Flow를 사용하면 일회성 이벤트(Toast 등) 처리가 더 용이할 것입니다.
- **DI(Dependency Injection) 사용**: `AppDatabase`나 `WeatherRepository`를 수동으로 가져오는 대신 Hilt나 Koin을 사용하면 의존성 관리가 더 깔끔해질 것입니다.

---

## Codex 코드 리뷰

### 아키텍처
`FavoriteCityRepository` 인터페이스를 정의하고 `RoomFavoriteCityRepository`로 구현체를 분리한 점이 돋보입니다. 이는 객체지향의 의존 역전 원칙(DIP)을 잘 따른 사례입니다. 다만, 많은 유틸리티 함수와 클래스들이 한 파일에 몰려 있는 경향이 있어, 파일 분리를 통해 응집도를 더 높일 수 있을 것 같습니다.

### 코루틴 · 상태 관리
ViewModel에서 Compose의 `mutableStateOf`를 사용하여 상태를 관리합니다. 이는 Compose UI와의 연동을 매우 단순하게 만들지만, UI 프레임워크에 대한 의존성이 생기는 단점이 있습니다. `runCatching` 블록을 사용하여 예외 처리를 깔끔하게 수행하고, 결과에 따라 상태를 업데이트하는 방식이 인상적입니다.

### Worker 구현 (V8)
`WeatherNotificationWorker`에서 Android 13(Tiramisu) 이상을 위한 알림 권한(`POST_NOTIFICATIONS`) 체크 로직을 포함한 점이 매우 훌륭합니다. 실무에서 놓치기 쉬운 부분을 정확히 짚어냈습니다. 다만, Worker 내부에서 실제 API를 호출하지 않고 `mockConditionForCity`로 데이터를 생성한 점은 실제 기능 구현 측면에서 아쉬움이 남습니다.

### 테스트 품질 (V5)
가장 놀라운 부분입니다. `FakeOpenMeteoServer`를 직접 구현하여 `URLStreamHandlerFactory`를 통해 네트워크 레이어 자체를 가로채서 테스트합니다. 이는 Retrofit이나 API 인터페이스만 모킹하는 것보다 훨씬 실제 상황에 가까운 통합 테스트를 가능하게 합니다. 테스트 가독성 또한 `awaitLoaded()` 같은 확장 함수를 사용하여 매우 뛰어납니다.

### DB 설계 (V7)
Room을 사용한 기본적인 설계를 따르고 있습니다. `FavoriteCityDao`에서 `getFavoriteCityIndex()`를 통해 간단하게 인덱스를 관리하며, `takeIf`를 사용하여 유효한 인덱스인지 검증하는 코틀린스러운(idiomatic) 코드를 작성했습니다.

### 잘 한 점 (내 코드보다 나은 부분)
- **테스트용 가짜 서버(`FakeOpenMeteoServer`) 구현**: 네트워크 요청/응답 과정을 완벽하게 시뮬레이션하여 테스트 신뢰도를 극대화함.
- **알림 권한 체크 로직**: `canPostNotifications()` 메서드를 통해 최신 안드로이드 OS 대응을 완벽히 함.
- **Repository 추상화**: 인터페이스를 사용하여 데이터 소스를 추상화한 덕분에 `NoOpFavoriteCityRepository` 같은 테스트용 객체 활용이 가능함.

### 개선 제안 (내가 유지보수한다면 바꿀 것)
- **ViewModel의 상태 관리 도구 변경**: Compose 전용 `mutableStateOf` 대신 플랫폼 독립적인 `StateFlow`를 사용하면 테스트와 재사용성 측면에서 더 유리할 것 같습니다.
- **Worker의 실제 데이터 사용**: Worker 내부에서도 실제 API 서비스를 호출하여 최신 날씨 정보를 알림으로 보내도록 개선이 필요합니다.
- **파일 분리**: `OpenMeteoWeatherRepository`나 각종 확장 함수들을 별도의 파일로 추출하여 `WeatherViewModel.kt` 파일의 크기를 줄이는 것이 좋겠습니다.

---

## 나의 코드 자가 개선 계획

두 코드를 리뷰한 결과, 내 코드에서 개선할 수 있는 구체적인 항목:

### 1. Repository 레이어 도입 및 관심사 분리
- **영감을 받은 코드**: Claude의 `WeatherRepository` 및 Codex의 `FavoriteCityRepository`
- **현재 내 코드 문제**: `WeatherViewModel.kt` 내부에 Retrofit 응답을 `WeatherData`로 변환하는 `mapToWeatherData` 로직과 API 호출 설정 로직이 직접 포함되어 있어 ViewModel이 너무 비대합니다.
- **제안하는 변경**: `WeatherRepository` 인터페이스와 그 구현체인 `WeatherRepositoryImpl`을 생성하여 API 호출 및 데이터 매핑 로직을 이동시키겠습니다.
- **예상 효과**: ViewModel의 책임이 UI 상태 관리로 한정되어 코드가 간결해지고, 데이터 소스 변경 시(예: 다른 API 사용) ViewModel을 수정할 필요가 없어집니다.
- **난이도**: medium

### 2. Worker 내 알림 권한 대응 및 재시도 로직 강화
- **영감을 받은 코드**: Codex의 `canPostNotifications()` 체크 로직
- **현재 내 코드 문제**: `WeatherWorker.kt`에서 알림 권한 체크 없이 바로 알림을 전송하려고 시도하며, API 호출 실패 시의 재시도 전략이 단순합니다.
- **제안하는 변경**: 알림 전송 전 권한 체크 로직을 추가하고, `Result.retry()` 호출 시 적절한 로그 기록과 함께 지수 백오프(Exponential Backoff) 정책을 고려하겠습니다.
- **예상 효과**: 안드로이드 13 이상 기기에서의 비정상 동작을 방지하고, 네트워크 불안정 상황에서도 안정적으로 알림을 보낼 수 있습니다.
- **난이도**: easy

### 3. 테스트 코드의 통합성 강화 (Fake Server 활용)
- **영감을 받은 코드**: Codex의 `FakeOpenMeteoServer`
- **현재 내 코드 문제**: `WeatherViewModelTest.kt`에서 `WeatherApiService` 인터페이스만 단순히 모킹하고 있어, 실제 HTTP 응답이나 파싱 과정에서 발생할 수 있는 오류를 검증하기 어렵습니다.
- **제안하는 변경**: `MockWebServer` 라이브러리를 도입하거나 Codex처럼 가짜 서버 로직을 구현하여 실제 JSON 응답을 통한 테스트를 수행하도록 개선하겠습니다.
- **예상 효과**: API 응답 필드 변경이나 파싱 오류를 테스트 단계에서 잡아낼 수 있어 앱의 안정성이 대폭 향상됩니다.
- **난이도**: hard

### 4. ViewModel 요청 중복 방지 로직 추가
- **영감을 받은 코드**: Claude의 `fetchJob` 관리 방식
- **현재 내 코드 문제**: `fetchWeatherData` 호출 시 이전 작업이 진행 중이더라도 이를 취소하지 않고 새 코루틴을 실행합니다.
- **제안하는 변경**: `private var fetchJob: Job? = null`을 선언하고, 새로운 날씨 호출 시 `fetchJob?.cancel()`을 먼저 수행하도록 수정하겠습니다.
- **예상 효과**: 불필요한 네트워크 요청을 방지하고, 마지막으로 요청한 도시의 데이터가 화면에 정확히 표시되도록 보장합니다.
- **난이도**: easy

---

## 전체 소감
다른 AI들의 코드를 리뷰하면서 각자가 가진 강점이 뚜렷하다는 점이 매우 놀라웠습니다. Claude는 안드로이드의 권장 아키텍처와 실무적인 코루틴 관리에 능숙했고, Codex는 객체지향적인 추상화와 매우 수준 높은 테스트 환경 구축 능력을 보여주었습니다. 특히 Codex의 `FakeOpenMeteoServer` 구현은 단순한 모킹을 넘어선 깊이 있는 접근이라 인상 깊었습니다.

나의 접근법 중 `StateFlow`와 `Turbine`을 활용한 상태 검증은 현대적인 안드로이드 개발 트렌드에 잘 부합함을 확인할 수 있었습니다. 하지만 데이터 레이어의 추상화가 부족하여 ViewModel에 너무 많은 짐을 지우고 있었다는 점은 반성하게 되었습니다. 만약 처음부터 다시 시작한다면, Codex처럼 Repository 인터페이스를 먼저 설계하고, Claude처럼 코루틴 생명주기를 더 정밀하게 제어하는 구조를 선택할 것입니다.

이번 리뷰 경험은 단순히 코드를 읽는 것을 넘어, 동일한 요구사항을 해결하는 다양한 설계 철학을 접할 수 있는 소중한 기회였습니다. "정답은 하나가 아니지만, 더 나은 구조는 분명히 존재한다"는 사실을 다시 한번 깨달았습니다.

소요 시간: 약 45분
