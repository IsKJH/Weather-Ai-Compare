package com.example.weathernow

import java.io.ByteArrayInputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection
import java.net.URLStreamHandler
import java.net.URLStreamHandlerFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class WeatherViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Before
    fun resetFakeServer() {
        FakeOpenMeteoServer.mode = FakeOpenMeteoServer.Mode.Success
    }

    @After
    fun tearDown() {
        FakeOpenMeteoServer.mode = FakeOpenMeteoServer.Mode.Success
    }

    @Test
    fun initialState_isLoadingOnStart() = runTest {
        val viewModel = WeatherViewModel()

        assertTrue(viewModel.uiState.isLoading)
        assertFalse(viewModel.uiState.isRefreshing)
        assertNull(viewModel.uiState.errorMessage)
    }

    @Test
    fun selectCity_changesSelectedCity() = runTest {
        val viewModel = WeatherViewModel()
        viewModel.awaitLoaded()

        viewModel.selectCity(1)

        assertEquals(1, viewModel.uiState.selectedCityIndex)
        assertEquals(supportedCities[1].name, viewModel.uiState.selectedWeather?.city)
    }

    @Test
    fun toggleFavorite_setsAndUnsetsSelectedCityAsFavorite() = runTest {
        val viewModel = WeatherViewModel()
        viewModel.awaitLoaded()
        viewModel.selectCity(2)

        viewModel.toggleFavorite()

        assertEquals(2, viewModel.uiState.favoriteCityIndex)
        assertTrue(viewModel.uiState.isSelectedCityFavorite)

        viewModel.toggleFavorite()

        assertNull(viewModel.uiState.favoriteCityIndex)
        assertFalse(viewModel.uiState.isSelectedCityFavorite)
    }

    @Test
    fun networkFailure_setsErrorAndClearsLoading() = runTest {
        FakeOpenMeteoServer.mode = FakeOpenMeteoServer.Mode.Failure

        val viewModel = WeatherViewModel()
        viewModel.awaitNotLoading()

        assertFalse(viewModel.uiState.isLoading)
        assertFalse(viewModel.uiState.isRefreshing)
        assertNotNull(viewModel.uiState.errorMessage)
        assertTrue(viewModel.uiState.weatherList.isEmpty())
    }

    @Test
    fun successfulFetch_clearsLoadingAndErrorStates() = runTest {
        val viewModel = WeatherViewModel()
        viewModel.awaitLoaded()

        assertFalse(viewModel.uiState.isLoading)
        assertFalse(viewModel.uiState.isRefreshing)
        assertNull(viewModel.uiState.errorMessage)
        assertEquals(supportedCities.size, viewModel.uiState.weatherList.size)
        assertEquals(supportedCities[0].name, viewModel.uiState.selectedWeather?.city)
    }

    private suspend fun WeatherViewModel.awaitLoaded() {
        awaitState { !uiState.isLoading && uiState.weatherList.size == supportedCities.size }
    }

    private suspend fun WeatherViewModel.awaitNotLoading() {
        awaitState { !uiState.isLoading }
    }

    private suspend fun awaitState(predicate: () -> Boolean) {
        withContext(Dispatchers.Default.limitedParallelism(1)) {
            withTimeout(5_000) {
                while (!predicate()) {
                    delay(10)
                }
            }
        }
    }

    companion object {
        @JvmStatic
        @BeforeClass
        fun installFakeOpenMeteoServer() {
            FakeOpenMeteoServer.install()
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

private object FakeOpenMeteoServer {
    @Volatile
    var mode: Mode = Mode.Success

    @Volatile
    private var installed = false

    enum class Mode {
        Success,
        Failure
    }

    fun install() {
        if (installed) return
        try {
            URL.setURLStreamHandlerFactory(FakeUrlStreamHandlerFactory)
        } catch (_: Error) {
            // Another test may have already installed a URL factory in this JVM.
        }
        installed = true
    }

    private object FakeUrlStreamHandlerFactory : URLStreamHandlerFactory {
        override fun createURLStreamHandler(protocol: String): URLStreamHandler? {
            return if (protocol == "https") FakeHttpsHandler else null
        }
    }

    private object FakeHttpsHandler : URLStreamHandler() {
        override fun openConnection(url: URL): URLConnection = FakeHttpURLConnection(url)
    }

    private class FakeHttpURLConnection(url: URL) : HttpURLConnection(url) {
        override fun connect() = Unit

        override fun disconnect() = Unit

        override fun usingProxy(): Boolean = false

        override fun getResponseCode(): Int {
            return if (mode == Mode.Failure) 500 else 200
        }

        override fun getInputStream(): InputStream {
            val body = when {
                url.host == "api.open-meteo.com" -> forecastResponse()
                url.host == "air-quality-api.open-meteo.com" -> airQualityResponse()
                else -> error("Unexpected URL: $url")
            }
            return ByteArrayInputStream(body.toByteArray())
        }
    }

    private fun forecastResponse(): String = """
        {
          "current": {
            "temperature_2m": 21.4,
            "relative_humidity_2m": 62,
            "apparent_temperature": 22.1,
            "weather_code": 1,
            "wind_speed_10m": 3.2
          },
          "hourly": {
            "time": [
              "2099-01-01T00:00",
              "2099-01-01T01:00",
              "2099-01-01T02:00",
              "2099-01-01T03:00",
              "2099-01-01T04:00",
              "2099-01-01T05:00",
              "2099-01-01T06:00",
              "2099-01-01T07:00"
            ],
            "temperature_2m": [21.0, 21.5, 22.0, 22.5, 23.0, 23.5, 24.0, 24.5],
            "weather_code": [1, 1, 2, 2, 3, 3, 0, 0],
            "precipitation_probability": [0, 0, 5, 5, 10, 10, 0, 0]
          },
          "daily": {
            "time": ["2099-01-01", "2099-01-02", "2099-01-03", "2099-01-04", "2099-01-05"],
            "weather_code": [1, 2, 3, 61, 0],
            "temperature_2m_max": [25.0, 26.0, 24.0, 22.0, 23.0],
            "temperature_2m_min": [18.0, 19.0, 17.0, 16.0, 17.0],
            "uv_index_max": [5.1, 4.2, 3.7, 2.1, 5.0],
            "precipitation_probability_max": [10, 20, 30, 40, 0]
          }
        }
    """.trimIndent()

    private fun airQualityResponse(): String = """
        {
          "current": {
            "european_aqi": 35
          }
        }
    """.trimIndent()
}
