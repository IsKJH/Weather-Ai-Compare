package com.example.weathernow

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class WeatherUiState(
    val selectedCityIndex: Int = 0,
    val weatherList: List<WeatherData> = mockWeatherList,
    val favoriteCityIndex: Int? = null,
    val isRefreshing: Boolean = false,
    val lastUpdated: String = currentUpdateTime(),
    val refreshCount: Int = 0
) {
    val selectedWeather: WeatherData = weatherList[selectedCityIndex]
    val isSelectedCityFavorite: Boolean = selectedCityIndex == favoriteCityIndex
}

class WeatherViewModel : ViewModel() {
    var uiState by mutableStateOf(WeatherUiState())
        private set

    fun selectCity(index: Int) {
        if (index in uiState.weatherList.indices) {
            uiState = uiState.copy(selectedCityIndex = index)
        }
    }

    fun toggleFavorite() {
        uiState = uiState.copy(
            favoriteCityIndex = if (uiState.isSelectedCityFavorite) null else uiState.selectedCityIndex
        )
    }

    fun refresh() {
        if (uiState.isRefreshing) return

        viewModelScope.launch {
            uiState = uiState.copy(isRefreshing = true)
            delay(700)
            val nextRefreshCount = uiState.refreshCount + 1
            uiState = uiState.copy(
                weatherList = refreshedWeather(nextRefreshCount),
                isRefreshing = false,
                lastUpdated = currentUpdateTime(),
                refreshCount = nextRefreshCount
            )
        }
    }
}

private fun refreshedWeather(refreshCount: Int): List<WeatherData> {
    val tempOffset = when (refreshCount % 3) {
        1 -> 1
        2 -> -1
        else -> 0
    }
    val precipitationOffset = (refreshCount % 2) * 5

    return mockWeatherList.map { weather ->
        weather.copy(
            currentTemp = weather.currentTemp + tempOffset,
            feelsLike = weather.feelsLike + tempOffset,
            precipitationProbability = (weather.precipitationProbability + precipitationOffset).coerceAtMost(95),
            hourlyForecast = weather.hourlyForecast.mapIndexed { index, hourly ->
                hourly.copy(temperature = hourly.temperature + tempOffset + if (index > 2) -1 else 0)
            }
        )
    }
}

private fun currentUpdateTime(): String {
    return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
}
