package com.example.weathernow

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val testDispatcher: TestDispatcher = StandardTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

class WeatherViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var apiService: WeatherApiService
    private lateinit var viewModel: WeatherViewModel

    @Before
    fun setup() {
        apiService = mockk()
        mockkObject(WeatherApiService.Companion)
        every { WeatherApiService.Companion.create() } returns apiService
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    private fun createMockResponse(): WeatherResponse {
        return WeatherResponse(
            current = CurrentWeather(
                temperature_2m = 25.0,
                relative_humidity_2m = 50,
                apparent_temperature = 26.0,
                precipitation = 0.0,
                weather_code = 0,
                wind_speed_10m = 5.0
            ),
            hourly = HourlyData(
                time = List(24) { "2023-01-01T${it.toString().padStart(2, '0')}:00" },
                temperature_2m = List(24) { 20.0 },
                weather_code = List(24) { 0 }
            ),
            daily = DailyData(
                time = List(7) { "2023-01-0${it + 1}" },
                weather_code = List(7) { 0 },
                temperature_2m_max = List(7) { 28.0 },
                temperature_2m_min = List(7) { 18.0 },
                uv_index_max = List(7) { 5.0 }
            )
        )
    }

    @Test
    fun `initial loading state is true on start`() = runTest {
        // Given
        coEvery { apiService.getForecast(any(), any()) } coAnswers {
            kotlinx.coroutines.delay(1000)
            createMockResponse()
        }

        // When
        viewModel = WeatherViewModel()

        // Then
        viewModel.uiState.test {
            // First item is the initial state (isLoading = false)
            assertEquals(false, awaitItem().isLoading)
            
            // Trigger coroutines to run up to the first delay/suspension
            runCurrent()
            
            // Second item should be isLoading = true
            assertTrue(awaitItem().isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `successful data fetch clears loading and error states`() = runTest {
        // Given
        val mockResponse = createMockResponse()
        coEvery { apiService.getForecast(any(), any()) } returns mockResponse

        // When
        viewModel = WeatherViewModel()

        // Then
        viewModel.uiState.test {
            awaitItem() // initial false
            runCurrent()
            assertTrue(awaitItem().isLoading) // loading true
            
            val successState = awaitItem()
            assertEquals("서울", successState.weatherData?.city)
            assertEquals(25, successState.weatherData?.currentTemp)
            assertEquals(false, successState.isLoading)
            assertNull(successState.error)
        }
    }

    @Test
    fun `error state is set when the network call fails`() = runTest {
        // Given
        coEvery { apiService.getForecast(any(), any()) } throws Exception("Network error")

        // When
        viewModel = WeatherViewModel()

        // Then
        viewModel.uiState.test {
            awaitItem() // initial false
            runCurrent()
            assertTrue(awaitItem().isLoading) // loading true

            val errorState = awaitItem()
            assertEquals(false, errorState.isLoading)
            assertTrue(errorState.error?.contains("Network error") == true)
        }
    }

    @Test
    fun `city selection changes the selected city`() = runTest {
        // Given
        coEvery { apiService.getForecast(any(), any()) } returns createMockResponse()
        viewModel = WeatherViewModel()

        // Consume initial states from "서울"
        viewModel.uiState.test {
            awaitItem() // initial false
            runCurrent()
            awaitItem() // loading true
            awaitItem() // success "서울"

            // When
            viewModel.selectCity("부산")
            runCurrent()

            // Then
            assertTrue(awaitItem().isLoading) // loading true for 부산
            val successState = awaitItem()
            assertEquals("부산", successState.weatherData?.city)
        }
    }

    @Test
    fun `favorite toggle works correctly (set and unset)`() = runTest {
        // Given
        coEvery { apiService.getForecast(any(), any()) } returns createMockResponse()
        viewModel = WeatherViewModel()

        viewModel.uiState.test {
            // Consume initial states
            awaitItem() // initial false
            runCurrent()
            awaitItem() // loading true
            awaitItem() // success "서울"

            // When toggle on
            viewModel.toggleFavorite("서울")
            assertEquals("서울", awaitItem().favoriteCity)

            // When toggle off
            viewModel.toggleFavorite("서울")
            assertNull(awaitItem().favoriteCity)
        }
    }
}
