package com.example.weathernow

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import kotlin.math.roundToInt

data class WeatherUiState(
    val selectedCityIndex: Int = 0,
    val cities: List<City> = supportedCities,
    val weatherList: List<WeatherData> = emptyList(),
    val favoriteCityIndex: Int? = null,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val lastUpdated: String = "--:--",
    val refreshCount: Int = 0
) {
    val selectedWeather: WeatherData? = weatherList.getOrNull(selectedCityIndex)
    val isSelectedCityFavorite: Boolean = selectedCityIndex == favoriteCityIndex
}

class WeatherViewModel : ViewModel() {
    private val repository = OpenMeteoWeatherRepository()

    var uiState by mutableStateOf(WeatherUiState())
        private set

    init {
        loadWeather(isRefresh = false)
    }

    fun selectCity(index: Int) {
        if (index in uiState.cities.indices) {
            uiState = uiState.copy(selectedCityIndex = index)
        }
    }

    fun toggleFavorite() {
        uiState = uiState.copy(
            favoriteCityIndex = if (uiState.isSelectedCityFavorite) null else uiState.selectedCityIndex
        )
    }

    fun refresh() {
        if (uiState.isLoading || uiState.isRefreshing) return
        loadWeather(isRefresh = true)
    }

    private fun loadWeather(isRefresh: Boolean) {
        viewModelScope.launch {
            uiState = uiState.copy(
                isLoading = !isRefresh && uiState.weatherList.isEmpty(),
                isRefreshing = isRefresh,
                errorMessage = null
            )

            runCatching {
                repository.fetchWeather(uiState.cities)
            }.onSuccess { weather ->
                uiState = uiState.copy(
                    weatherList = weather,
                    isLoading = false,
                    isRefreshing = false,
                    errorMessage = null,
                    lastUpdated = currentUpdateTime(),
                    refreshCount = uiState.refreshCount + if (isRefresh) 1 else 0
                )
            }.onFailure { error ->
                uiState = uiState.copy(
                    isLoading = false,
                    isRefreshing = false,
                    errorMessage = "날씨 정보를 불러오지 못했습니다. ${error.message ?: "네트워크 상태를 확인해 주세요."}"
                )
            }
        }
    }
}

private class OpenMeteoWeatherRepository {
    suspend fun fetchWeather(cities: List<City>): List<WeatherData> = withContext(Dispatchers.IO) {
        cities.map { city ->
            async { fetchCityWeather(city) }
        }.awaitAll()
    }

    private fun fetchCityWeather(city: City): WeatherData {
        val url = URL(
            "https://api.open-meteo.com/v1/forecast" +
                "?latitude=${city.latitude}" +
                "&longitude=${city.longitude}" +
                "&current=temperature_2m,relative_humidity_2m,apparent_temperature,weather_code,wind_speed_10m" +
                "&hourly=temperature_2m,weather_code,precipitation_probability" +
                "&daily=weather_code,temperature_2m_max,temperature_2m_min,uv_index_max,precipitation_probability_max" +
                "&forecast_days=5" +
                "&timezone=Asia%2FSeoul" +
                "&wind_speed_unit=ms"
        )

        val json = JSONObject(url.readTextWithTimeout())
        val current = json.getJSONObject("current")
        val hourly = json.getJSONObject("hourly")
        val daily = json.getJSONObject("daily")
        val currentCode = current.getInt("weather_code")

        return WeatherData(
            city = city.name,
            currentTemp = current.getDouble("temperature_2m").roundToInt(),
            feelsLike = current.getDouble("apparent_temperature").roundToInt(),
            condition = weatherCondition(currentCode),
            icon = weatherIcon(currentCode),
            humidity = current.getInt("relative_humidity_2m"),
            windSpeed = roundOneDecimal(current.getDouble("wind_speed_10m")),
            precipitationProbability = daily.getJSONArray("precipitation_probability_max").optInt(0, 0),
            uvIndex = daily.getJSONArray("uv_index_max").optDouble(0, 0.0).roundToInt(),
            airQuality = fetchAirQuality(city),
            hourlyForecast = parseHourlyForecast(hourly),
            forecast = parseDailyForecast(daily)
        )
    }

    private fun fetchAirQuality(city: City): String {
        val url = URL(
            "https://air-quality-api.open-meteo.com/v1/air-quality" +
                "?latitude=${city.latitude}" +
                "&longitude=${city.longitude}" +
                "&current=european_aqi" +
                "&timezone=Asia%2FSeoul"
        )
        val current = JSONObject(url.readTextWithTimeout()).getJSONObject("current")
        val aqi = current.getInt("european_aqi")
        return "${airQualityLabel(aqi)} ($aqi)"
    }

    private fun parseHourlyForecast(hourly: JSONObject): List<HourlyForecast> {
        val times = hourly.getJSONArray("time")
        val temps = hourly.getJSONArray("temperature_2m")
        val codes = hourly.getJSONArray("weather_code")
        val now = LocalDateTime.now()

        return (0 until times.length())
            .map { index -> index to LocalDateTime.parse(times.getString(index)) }
            .filter { (_, time) -> !time.isBefore(now.minusMinutes(30)) }
            .take(8)
            .map { (index, time) ->
                val code = codes.getInt(index)
                HourlyForecast(
                    time = time.format(DateTimeFormatter.ofPattern("HH시")),
                    condition = weatherCondition(code),
                    icon = weatherIcon(code),
                    temperature = temps.getDouble(index).roundToInt()
                )
            }
    }

    private fun parseDailyForecast(daily: JSONObject): List<ForecastDay> {
        val times = daily.getJSONArray("time")
        val codes = daily.getJSONArray("weather_code")
        val highs = daily.getJSONArray("temperature_2m_max")
        val lows = daily.getJSONArray("temperature_2m_min")
        val today = LocalDate.now()

        return (0 until times.length()).map { index ->
            val date = LocalDate.parse(times.getString(index))
            val code = codes.getInt(index)
            ForecastDay(
                day = when (date) {
                    today -> "오늘"
                    today.plusDays(1) -> "내일"
                    else -> date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN)
                },
                condition = weatherCondition(code),
                icon = weatherIcon(code),
                high = highs.getDouble(index).roundToInt(),
                low = lows.getDouble(index).roundToInt()
            )
        }
    }
}

private fun URL.readTextWithTimeout(): String {
    val connection = (openConnection() as HttpURLConnection).apply {
        connectTimeout = 10_000
        readTimeout = 10_000
        requestMethod = "GET"
    }

    return try {
        val responseCode = connection.responseCode
        if (responseCode !in 200..299) {
            throw IllegalStateException("Open-Meteo 응답 코드: $responseCode")
        }
        connection.inputStream.bufferedReader().use { it.readText() }
    } finally {
        connection.disconnect()
    }
}

private fun weatherCondition(code: Int): String = when (code) {
    0 -> "맑음"
    1 -> "구름조금"
    2 -> "구름많음"
    3 -> "흐림"
    45, 48 -> "안개"
    in 51..55 -> "이슬비"
    in 61..65 -> "비"
    in 71..75 -> "눈"
    in 80..82 -> "소나기"
    95, 96, 99 -> "뇌우"
    else -> "알 수 없음"
}

private fun weatherIcon(code: Int): String = when (code) {
    0 -> "☀"
    1 -> "🌤"
    2 -> "⛅"
    3 -> "☁"
    45, 48 -> "🌫"
    in 51..55 -> "🌦"
    in 61..65 -> "🌧"
    in 71..75 -> "❄"
    in 80..82 -> "🌦"
    95, 96, 99 -> "⛈"
    else -> "?"
}

private fun airQualityLabel(aqi: Int): String = when {
    aqi <= 20 -> "좋음"
    aqi <= 40 -> "양호"
    aqi <= 60 -> "보통"
    aqi <= 80 -> "나쁨"
    aqi <= 100 -> "매우 나쁨"
    else -> "위험"
}

private fun roundOneDecimal(value: Double): Double = (value * 10.0).roundToInt() / 10.0

private fun currentUpdateTime(): String {
    return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
}
