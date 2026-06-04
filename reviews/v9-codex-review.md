# V9 코드 리뷰 - Codex의 시각

검토 시간: 2026-06-04 10:07 ~ 2026-06-04 10:18 KST

## Claude 코드 리뷰

### 아키텍처
Claude 구현은 `WeatherViewModel`, `WeatherRepository`, `AppDatabase`, `WeatherWorker`가 역할별로 분리되어 있어 전체 구조가 단순하고 읽기 쉽다. `WeatherRepository`가 Open-Meteo 날씨 API와 대기질 API 호출, JSON 파싱, 도메인 모델 변환을 모두 담당하므로 ViewModel 쪽의 책임은 비교적 작다. 다만 `WeatherRepository`가 `object` 싱글톤이고 `WeatherViewModel`이 이를 직접 호출하기 때문에 네트워크 계층을 교체하거나 테스트용 fake를 주입하는 구조는 약하다. DB도 `AppDatabase.getInstance(context)` 형태의 전형적인 싱글톤이라 앱 코드에서는 편하지만 Worker 테스트에서는 의존성 대체가 어렵다.

### 코루틴 및 상태 관리
`WeatherViewModel`은 `MutableStateFlow<WeatherUiState>`와 `asStateFlow()`를 사용해 UI 상태를 명확하게 노출한다. `fetchJob?.cancel()` 후 새 `viewModelScope.launch`를 시작하는 방식은 도시를 빠르게 바꿀 때 오래된 요청이 뒤늦게 UI를 덮어쓰는 문제를 줄인다. `selectCity(index)`에서 인덱스 범위 검사가 없어 잘못된 값이 들어오면 `CITIES[cityIndex]` 접근에서 예외가 날 수 있는 점은 보완이 필요하다. `toggleFavorite()`는 UI 상태를 먼저 낙관적으로 바꾸고 DAO 저장을 비동기로 수행하는데, DB 실패 시 UI와 저장 상태가 달라질 수 있다.

### Worker 구현 (V8)
`WeatherWorker`는 `CoroutineWorker` 안에서 `AppDatabase.getInstance(applicationContext).favoriteCityDao()`로 저장된 즐겨찾기 도시를 읽고, `WeatherRepository.fetchWeather(city)`로 실제 최신 날씨를 가져온 뒤 알림을 만든다. 즐겨찾기가 없거나 저장된 `cityIndex`가 `CITIES.indices` 범위를 벗어나면 `Result.success()`로 조용히 종료하는 처리는 합리적이다. 네트워크 실패는 `Result.retry()`로 돌려 WorkManager 재시도 정책을 활용한다. 다만 알림 채널 생성 책임이 이 파일에는 없고, Android 13 이상의 `POST_NOTIFICATIONS` 권한 확인도 없어서 실제 알림 표시 안정성은 앱의 다른 부분에 의존한다.

### 테스트 전략 (V5)
`WeatherViewModelTest`는 초기 로딩, 성공 fetch, 실패 fetch, `selectCity()`, `toggleFavorite()`를 다뤄 ViewModel의 핵심 상태 전이를 잘 확인한다. `mockkObject(WeatherRepository)`로 싱글톤 repository를 mock 처리해 빠르게 테스트할 수 있지만, 전역 mock은 테스트 간 격리와 장기 유지보수 측면에서 비용이 있다. DAO가 nullable인 `WeatherViewModel(favoriteCityDao: FavoriteCityDao? = null)` 덕분에 즐겨찾기 테스트는 DB 없이 가능하지만, 저장 성공/실패나 `loadSavedFavorite()`의 복원 흐름은 충분히 검증되지 않는다. 테스트 이름과 의도는 좋지만, 현재 파일의 한글 문자열이 깨져 있어 실패 메시지 가독성이 낮다.

### DB 설계 (V7)
`AppDatabase`는 `FavoriteCityEntity`만 포함한 Room DB이고 `favoriteCityDao()`를 노출하는 최소 설계다. `@Database(..., version = 1, exportSchema = false)` 설정은 실험 앱에서는 단순하지만, 장기적으로는 스키마 내보내기와 마이그레이션 테스트가 빠져 있다. 싱글톤 생성은 `@Volatile`과 `synchronized`를 사용해 일반적인 안전성을 확보했다. 다만 Claude 코드의 favorite 저장은 city name이 아니라 index 기반이라 `CITIES` 순서가 바뀌면 이전 즐겨찾기가 다른 도시로 해석될 수 있다.

### 좋은 점 (Codex 코드보다 나은 부분)
- `WeatherViewModel.fetchWeather(cityIndex)`가 `fetchJob?.cancel()`을 사용해 이전 네트워크 요청을 취소하는 점은 Codex의 `WeatherViewModel.loadWeather()`보다 경쟁 상태에 강하다.
- `WeatherWorker.doWork()`가 `WeatherRepository.fetchWeather(city)`로 실제 날씨를 조회해 알림을 만드는 점은 Codex의 `WeatherNotificationWorker.mockConditionForCity()`보다 기능적으로 정확하다.
- `WeatherRepository.fetchWeather(city)`가 날씨 API와 대기질 API를 `async`로 병렬 호출하고, 대기질 실패만 fallback 처리하는 점은 사용자 경험과 네트워크 효율 면에서 좋다.

### 개선 제안 (Codex가 유지보수한다면 바꿀 것)
- `WeatherViewModel.selectCity(index)`에 `if (index !in CITIES.indices) return` 또는 오류 상태 처리를 추가해 잘못된 index 입력을 방어하겠다.
- `WeatherRepository`를 `object`에서 인터페이스 기반 의존성으로 바꾸고 `WeatherViewModel` 생성자에 주입해 `mockkObject` 없이 fake repository로 테스트하겠다.
- `FavoriteCityEntity`에는 index 대신 stable city id 또는 city name을 저장해 `CITIES` 순서 변경에 안전하게 만들겠다.

---

## Gemini 코드 리뷰

### 아키텍처
Gemini 구현은 `WeatherViewModel`, `WeatherApiService`, `FavoriteDatabase.kt`, `WeatherWorker`가 한 패키지 중심으로 구성되어 진입 장벽은 낮다. Retrofit 기반 `WeatherApiService`와 DTO인 `WeatherResponse`, `CurrentWeather`, `HourlyData`, `DailyData`를 둔 점은 원시 `URL`/`JSONObject` 방식보다 API 모델이 명확하다. 하지만 `WeatherViewModel` 내부에서 `private val apiService = WeatherApiService.create()`를 직접 생성하므로 생성자 주입이 되지 않고, 테스트는 `WeatherApiService.Companion.create()`를 mock하는 우회 방식을 쓴다. `FavoriteDatabase.kt` 한 파일에 Entity, DAO, Database가 모두 들어 있어 작은 앱에는 간편하지만 파일 책임 분리는 약하다.

### 코루틴 및 상태 관리
`WeatherViewModel`은 `MutableStateFlow<WeatherUiState>`와 `update {}`를 사용해 상태 변경을 간결하게 표현한다. `loadFavoriteCityAndDefault()`에서 저장된 즐겨찾기를 읽고 곧바로 `selectCity(savedFavorite ?: "서울")`을 호출하는 흐름은 초기 화면을 자연스럽게 구성한다. 반면 `fetchWeatherData(city)`는 새 요청을 시작할 때 이전 요청을 취소하지 않아 사용자가 도시를 빠르게 바꾸면 늦게 끝난 요청이 최신 선택을 덮을 수 있다. UI 상태에는 선택 도시 index나 city name이 별도 필드로 없고 `weatherData?.city`에 의존하므로 로딩 중 선택 상태 표현이 다소 불명확하다.

### Worker 구현 (V8)
`WeatherWorker.doWork()`는 DB에서 `FavoriteCity`를 읽고, `WeatherApiService.create().getForecast()`로 실제 데이터를 조회한 뒤 알림을 표시한다. 실패 시 전체를 `try/catch`로 감싸 `Result.retry()`를 반환하는 기본 흐름은 WorkManager와 잘 맞는다. 다만 `mapWeatherCode()`가 `WeatherViewModel`의 매핑 로직과 중복되어 있고, 주석에도 helper copied라고 되어 있어 변경 시 불일치 위험이 크다. `showNotification()`은 채널 id 문자열 `"weather_notifications"`를 직접 쓰고 권한 확인도 없어 Android 13 이상 환경에서 실패 가능성이 있다.

### 테스트 전략 (V5)
`WeatherViewModelTest`는 Turbine을 사용해 `StateFlow` 방출 순서를 관찰하므로 단순 최종 상태 검증보다 상태 전이 검증력이 좋다. `MainDispatcherRule`을 별도 `TestWatcher`로 둔 점도 반복 코드를 줄인다. 그러나 `WeatherViewModel`이 `WeatherApiService.create()`를 직접 호출하기 때문에 테스트가 `mockkObject(WeatherApiService.Companion)`에 의존하고, 생성자 주입을 사용하는 구조보다 취약하다. 테스트 데이터와 assertion 문자열 일부가 깨져 있어 `"서울"`, `"부산"` 같은 값 검증의 의도를 읽기 어렵다.

### DB 설계 (V7)
`FavoriteDatabase.kt`의 `FavoriteCity`, `FavoriteCityDao`, `WeatherDatabase`는 단일 즐겨찾기 도시를 저장하기에 충분히 단순하다. `FavoriteCity`가 `cityName`을 저장하므로 index 저장 방식보다 도시 목록 순서 변경에는 안전하다. 하지만 `cityName: String?`로 nullable을 허용하면서 `deleteFavoriteCity()`도 별도로 있어, 즐겨찾기 없음 상태를 null row와 row 삭제 중 어느 방식으로 표현할지 애매하다. `@Database`에 `exportSchema` 설정이 없고 마이그레이션 전략도 없어 스키마 변경 대비는 부족하다.

### 좋은 점 (Codex 코드보다 나은 부분)
- `WeatherApiService`가 Retrofit 인터페이스와 DTO를 사용해 API 계약을 명시한 점은 Codex의 `OpenMeteoWeatherRepository` 내부 `JSONObject` 파싱보다 구조적으로 선명하다.
- `WeatherViewModel.generateInsight(data)`로 현재 날씨에 따른 `smartInsight`를 만드는 점은 Codex UI 상태보다 사용자에게 더 해석된 정보를 제공한다.
- `WeatherViewModelTest`가 Turbine으로 `StateFlow`의 중간 상태를 검증하는 점은 Codex의 polling 기반 `awaitState`보다 상태 흐름을 더 직접적으로 검증한다.

### 개선 제안 (Codex가 유지보수한다면 바꿀 것)
- `WeatherViewModel` 생성자에 `WeatherApiService` 또는 `WeatherRepository`를 주입해 `WeatherApiService.Companion.create()` mock을 제거하겠다.
- `mapWeatherCode()`를 ViewModel과 Worker가 공유하는 별도 mapper로 이동해 중복과 불일치 위험을 줄이겠다.
- `FavoriteCity.cityName`을 non-null로 만들고 즐겨찾기 해제는 `deleteFavoriteCity()`만 사용하도록 상태 표현을 하나로 고정하겠다.

---

## Codex 코드 자가 개선 계획

다른 코드를 리뷰한 결과, Codex 코드에서 개선할 수 있는 구체적인 항목은 다음과 같다.

### 1. WeatherRepository 의존성 주입 도입
- 영감을 받은 코드: Gemini의 `WeatherApiService` 인터페이스와 DTO 분리, Claude 테스트의 repository mock 전략.
- 현재 Codex 코드 문제: `WeatherViewModel.kt` 안에서 `private val repository = OpenMeteoWeatherRepository()`를 직접 만들고, `OpenMeteoWeatherRepository`도 private class라 테스트가 URL 전역 handler인 `FakeOpenMeteoServer`에 의존한다.
- 제안하는 변경: `interface WeatherRepository { suspend fun fetchWeather(cities: List<City>): List<WeatherData> }`를 만들고 `OpenMeteoWeatherRepository`를 별도 파일로 이동한 뒤, `WeatherViewModel(private val weatherRepository: WeatherRepository, private val favoriteCityRepository: FavoriteCityRepository)` 형태로 주입한다.
- 예상 효과: ViewModel 테스트가 네트워크 파싱과 분리되고, 성공/실패/느린 응답/부분 실패를 fake repository로 더 정확하고 빠르게 검증할 수 있다.
- 난이도: medium

### 2. 도시 선택 요청의 경쟁 상태 방지
- 영감을 받은 코드: Claude의 `WeatherViewModel.fetchWeather()`가 `fetchJob?.cancel()`로 이전 요청을 취소하는 부분.
- 현재 Codex 코드 문제: `WeatherViewModel.loadWeather()`는 전체 도시 목록을 한 번에 가져오는 구조라 도시 선택 자체는 네트워크를 재시작하지 않지만, `refresh()` 중에 상태가 바뀌거나 향후 단일 도시 fetch로 바뀔 경우 이전 작업을 관리할 수 있는 장치가 없다.
- 제안하는 변경: `private var loadWeatherJob: Job?`를 추가하고 `loadWeather(isRefresh)` 시작 시 기존 job을 취소하거나, 최소한 refresh 중복 방지 외에 최신 요청 token을 두어 오래된 결과가 `uiState`를 덮어쓰지 못하게 한다.
- 예상 효과: 빠른 refresh, 화면 재진입, 향후 단일 도시 fetch 확장에서도 UI 상태가 최신 사용자 의도와 일치한다.
- 난이도: easy

### 3. 알림 Worker에서 실제 날씨 조회 사용
- 영감을 받은 코드: Claude의 `WeatherWorker.doWork()`와 Gemini의 `WeatherWorker.doWork()`가 저장된 즐겨찾기 도시의 실제 날씨 API 결과를 알림에 사용하는 부분.
- 현재 Codex 코드 문제: `WeatherNotificationWorker.kt`의 `mockConditionForCity(cityIndex)`가 고정 문자열을 반환하므로 알림이 실제 날씨와 무관하다.
- 제안하는 변경: `WeatherNotificationWorker`가 주입 가능한 repository 또는 `OpenMeteoWeatherRepository`를 통해 `supportedCities[favoriteCityIndex]`의 실제 `WeatherData`를 가져오고, `condition`, `currentTemp`, `airQuality`를 알림 본문에 넣는다. 실패 시에는 네트워크 오류는 `Result.retry()`, 권한 없음과 즐겨찾기 없음은 `Result.success()`로 구분한다.
- 예상 효과: V8 알림 기능이 데모용 mock이 아니라 실제 사용자 가치가 있는 기능이 된다.
- 난이도: medium

### 4. 즐겨찾기 해제 저장 방식 명확화
- 영감을 받은 코드: Claude의 `FavoriteCityDao.clear()`와 Gemini의 `deleteFavoriteCity()`처럼 즐겨찾기 없음 상태를 row 삭제로 표현하는 방식.
- 현재 Codex 코드 문제: `RoomFavoriteCityRepository.saveFavoriteCityIndex(index: Int?)`가 `FavoriteCityEntity(cityIndex = null)`을 저장해 즐겨찾기 없음 상태를 null column으로 표현한다. `FavoriteCityDao`에는 삭제 쿼리가 없어서 DB에는 항상 id 0 row가 남을 수 있다.
- 제안하는 변경: `FavoriteCityDao`에 `@Query("DELETE FROM favorite_city_preference WHERE id = :id") suspend fun clearFavoriteCity(id: Int = FAVORITE_CITY_ROW_ID)`를 추가하고, `RoomFavoriteCityRepository.saveFavoriteCityIndex(null)`에서는 저장 대신 삭제를 호출한다.
- 예상 효과: DB 상태가 더 직관적이고, 즐겨찾기 없음과 손상된 null 데이터가 구분된다.
- 난이도: easy

### 5. StateFlow 기반 UI 상태로 전환 검토
- 영감을 받은 코드: Claude와 Gemini가 모두 `MutableStateFlow<WeatherUiState>`를 사용해 UI 상태를 노출하는 부분.
- 현재 Codex 코드 문제: `WeatherViewModel`은 Compose `mutableStateOf(WeatherUiState())`를 직접 사용한다. Compose UI에는 편하지만, ViewModel 상태 관찰 테스트와 비 Compose 소비자 관점에서는 `StateFlow`보다 범용성이 낮다.
- 제안하는 변경: 내부 상태를 `MutableStateFlow`로 바꾸고 `val uiState: StateFlow<WeatherUiState>`를 노출하되, Compose 화면에서는 `collectAsStateWithLifecycle()`로 구독한다.
- 예상 효과: 테스트에서 Turbine을 사용할 수 있고, 상태 방출 순서와 중간 로딩 상태를 더 정밀하게 검증할 수 있다.
- 난이도: medium

---

## 전체 소감

Claude 코드는 전체적으로 작고 안정적인 구조를 유지하면서도 `fetchJob` 취소와 실제 날씨 기반 Worker처럼 실전적인 디테일이 있었다. Gemini 코드는 Retrofit DTO와 Turbine 테스트처럼 도구 선택이 현대적이고, `smartInsight`처럼 날씨 데이터를 사용자 문장으로 해석하는 시도가 인상적이었다. Codex 코드는 여러 도시를 병렬로 가져오고 권한 확인을 포함한 알림 안전성은 챙겼지만, repository 주입과 실제 알림 데이터 측면에서는 두 구현에서 배울 점이 분명했다.

Codex의 접근법 중 확인된 장점은 `FavoriteCityRepository` 인터페이스를 이미 두어 DB 의존성 일부를 분리했고, `WeatherNotificationWorker.canPostNotifications()`로 Android 13 권한 문제를 고려했다는 점이다. 반대로 `OpenMeteoWeatherRepository`를 ViewModel 파일 안에 private 구현으로 둔 선택은 테스트와 Worker 재사용을 어렵게 만들었다. 처음부터 다시 시작한다면 Retrofit 또는 별도 repository 인터페이스를 먼저 세우고, ViewModel과 Worker가 같은 날씨 조회 계층을 공유하도록 설계하겠다.
