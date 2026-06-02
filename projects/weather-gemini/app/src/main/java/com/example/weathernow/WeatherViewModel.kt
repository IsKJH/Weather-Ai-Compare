package com.example.weathernow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weathernow.data.FavoriteCity
import com.example.weathernow.data.FavoriteCityDao
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class WeatherUiState(
    val weatherData: WeatherData? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val favoriteCity: String? = null,
    val lastUpdated: String = "",
    val smartInsight: String = ""
)

class WeatherViewModel(private val favoriteCityDao: FavoriteCityDao) : ViewModel() {
    private val apiService = WeatherApiService.create()
    
    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    init {
        loadFavoriteCityAndDefault()
    }

    private fun loadFavoriteCityAndDefault() {
        viewModelScope.launch {
            val savedFavorite = favoriteCityDao.getFavoriteCity()?.cityName
            _uiState.update { it.copy(favoriteCity = savedFavorite) }
            selectCity(savedFavorite ?: "서울")
        }
    }

    fun selectCity(cityName: String) {
        val city = cities.find { it.name == cityName } ?: return
        fetchWeatherData(city)
    }

    fun refresh() {
        val currentCity = _uiState.value.weatherData?.city ?: "서울"
        selectCity(currentCity)
    }

    private fun fetchWeatherData(city: City) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val response = apiService.getForecast(city.latitude, city.longitude)
                val mappedData = mapToWeatherData(city.name, response)
                _uiState.update {
                    it.copy(
                        weatherData = mappedData,
                        isLoading = false,
                        error = null,
                        lastUpdated = getCurrentTime(),
                        smartInsight = generateInsight(mappedData)
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "날씨 정보를 불러오는 데 실패했습니다: ${e.message}"
                    )
                }
            }
        }
    }

    private fun mapToWeatherData(cityName: String, response: WeatherResponse): WeatherData {
        val current = response.current
        val daily = response.daily
        val hourly = response.hourly

        val forecast = daily.time.take(5).mapIndexed { index, time ->
            val dayOfWeek = getDayOfWeek(time)
            ForecastDay(
                day = dayOfWeek,
                condition = mapWeatherCode(daily.weather_code[index]),
                high = daily.temperature_2m_max[index].toInt(),
                low = daily.temperature_2m_min[index].toInt()
            )
        }

        val hourlyForecast = hourly.time.take(6).mapIndexed { index, time ->
            HourlyForecast(
                time = time.substring(11, 16),
                condition = mapWeatherCode(hourly.weather_code[index]),
                temp = hourly.temperature_2m[index].toInt()
            )
        }

        return WeatherData(
            city = cityName,
            currentTemp = current.temperature_2m.toInt(),
            feelsLike = current.apparent_temperature.toInt(),
            condition = mapWeatherCode(current.weather_code),
            humidity = current.relative_humidity_2m,
            windSpeed = current.wind_speed_10m,
            precipitation = current.precipitation.toInt(),
            uvIndex = daily.uv_index_max[0].toInt(),
            airQuality = "좋음", // Open-Meteo 기본 API에는 미세먼지 정보가 없으므로 고정값 사용
            forecast = forecast,
            hourlyForecast = hourlyForecast
        )
    }

    private fun getDayOfWeek(dateStr: String): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
        val date = sdf.parse(dateStr) ?: return ""
        val cal = Calendar.getInstance()
        cal.time = date
        return when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "월"
            Calendar.TUESDAY -> "화"
            Calendar.WEDNESDAY -> "수"
            Calendar.THURSDAY -> "목"
            Calendar.FRIDAY -> "금"
            Calendar.SATURDAY -> "토"
            Calendar.SUNDAY -> "일"
            else -> ""
        }
    }

    private fun generateInsight(data: WeatherData): String {
        val city = data.city
        val temp = data.currentTemp
        val condition = data.condition
        return when {
            temp > 28 -> "${city}은 무더운 날씨예요. 시원한 음료와 함께 휴식을 취하세요! 🍹"
            temp < 15 -> "${city}은 꽤 쌀쌀하네요. 따뜻한 옷차림으로 건강 유의하세요! 🧣"
            condition.contains("비") -> "비 소식이 있습니다. 외출 시 우산을 꼭 챙기세요! ☔"
            condition.contains("흐림") -> "하늘에 구름이 많네요. 실내 활동을 즐기기에 좋은 날이에요. ☕"
            else -> "기분 좋은 ${city}의 날씨입니다. 활기찬 하루 보내세요! ✨"
        }
    }

    fun toggleFavorite(city: String) {
        viewModelScope.launch {
            val isCurrentFavorite = _uiState.value.favoriteCity == city
            if (isCurrentFavorite) {
                favoriteCityDao.deleteFavoriteCity()
                _uiState.update { it.copy(favoriteCity = null) }
            } else {
                favoriteCityDao.setFavoriteCity(FavoriteCity(id = 1, cityName = city))
                _uiState.update { it.copy(favoriteCity = city) }
            }
        }
    }

    private fun getCurrentTime(): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.KOREA)
        return sdf.format(Date())
    }
}
