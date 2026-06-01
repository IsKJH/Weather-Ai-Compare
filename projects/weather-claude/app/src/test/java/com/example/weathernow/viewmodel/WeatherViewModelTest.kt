package com.example.weathernow.viewmodel

import com.example.weathernow.data.ForecastDay
import com.example.weathernow.data.HourlyForecast
import com.example.weathernow.data.WeatherData
import com.example.weathernow.data.WeatherRepository
import io.mockk.coEvery
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WeatherViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val fakeWeatherData = WeatherData(
        city = "서울",
        currentTemp = 20,
        feelsLike = 18,
        condition = "맑음",
        humidity = 60,
        windSpeed = 3.5,
        forecast = listOf(ForecastDay("월", "맑음", 22, 15)),
        hourlyForecast = listOf(HourlyForecast("12시", "맑음", 20)),
        precipitationProbability = 10,
        uvIndex = 3,
        airQuality = "보통"
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockkObject(WeatherRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    // Test 1: init 직후 isLoading = true 인지 확인
    @Test
    fun `initial state has isLoading true before fetch completes`() = runTest {
        // Repository를 무한 대기하게 만들어 로딩 상태를 유지
        coEvery { WeatherRepository.fetchWeather(any()) } coAnswers {
            delay(Long.MAX_VALUE)
            fakeWeatherData
        }

        val viewModel = WeatherViewModel()

        // StandardTestDispatcher: advanceUntilIdle() 없이는 코루틴이 실행 안 됨.
        // WeatherUiState 기본값이 isLoading=true 이고, init의 fetchWeather도 아직 실행 안 됨.
        assertTrue("init 직후 isLoading은 true여야 합니다", viewModel.uiState.value.isLoading)
    }

    // Test 2: 데이터 성공 시 isLoading=false, error=null, weatherData 설정
    @Test
    fun `successful fetch clears loading and error and sets weather data`() = runTest {
        coEvery { WeatherRepository.fetchWeather(any()) } returns fakeWeatherData

        val viewModel = WeatherViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse("성공 후 isLoading은 false여야 합니다", state.isLoading)
        assertNull("성공 후 error는 null이어야 합니다", state.error)
        assertEquals("weatherData가 올바르게 설정되어야 합니다", fakeWeatherData, state.weatherData)
    }

    // Test 3: 네트워크 오류 시 error 상태 설정
    @Test
    fun `error state is set when fetch throws an exception`() = runTest {
        coEvery { WeatherRepository.fetchWeather(any()) } throws RuntimeException("Network error")

        val viewModel = WeatherViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse("오류 후 isLoading은 false여야 합니다", state.isLoading)
        assertNotNull("오류 후 error가 설정되어야 합니다", state.error)
        assertTrue("error 메시지가 비어 있지 않아야 합니다", state.error!!.isNotBlank())
    }

    // Test 4: selectCity() 호출 시 selectedIndex 변경
    @Test
    fun `selectCity changes the selected city index`() = runTest {
        coEvery { WeatherRepository.fetchWeather(any()) } returns fakeWeatherData

        val viewModel = WeatherViewModel()
        advanceUntilIdle()

        assertEquals("초기 selectedIndex는 0이어야 합니다", 0, viewModel.uiState.value.selectedIndex)

        viewModel.selectCity(2)
        advanceUntilIdle()

        assertEquals("selectCity(2) 후 selectedIndex는 2여야 합니다", 2, viewModel.uiState.value.selectedIndex)
    }

    // Test 5: toggleFavorite() - 즐겨찾기 설정 및 해제
    @Test
    fun `toggleFavorite sets and then unsets the favorite city`() = runTest {
        coEvery { WeatherRepository.fetchWeather(any()) } returns fakeWeatherData

        val viewModel = WeatherViewModel()
        advanceUntilIdle()

        assertNull("초기에 즐겨찾기 도시는 없어야 합니다", viewModel.uiState.value.favoriteCityIndex)

        viewModel.toggleFavorite()
        assertEquals(
            "첫 번째 토글 후 현재 도시(0)가 즐겨찾기여야 합니다",
            0,
            viewModel.uiState.value.favoriteCityIndex
        )

        viewModel.toggleFavorite()
        assertNull(
            "두 번째 토글 후 즐겨찾기가 해제되어야 합니다",
            viewModel.uiState.value.favoriteCityIndex
        )
    }
}
