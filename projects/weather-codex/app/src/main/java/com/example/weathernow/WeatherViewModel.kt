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
    val lastUpdated: String = currentUpdateTime(),
    val isRefreshing: Boolean = false,
    val refreshCount: Int = 0
) {
    val selectedWeather: WeatherData = weatherList[selectedCityIndex]
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
        val newFavorite = if (uiState.favoriteCityIndex == uiState.selectedCityIndex) {
            null
        } else {
            uiState.selectedCityIndex
        }
        uiState = uiState.copy(favoriteCityIndex = newFavorite)
    }

    fun refresh() {
        if (uiState.isRefreshing) return

        viewModelScope.launch {
            uiState = uiState.copy(isRefreshing = true)
            delay(650)

            val nextRefreshCount = uiState.refreshCount + 1
            uiState = uiState.copy(
                weatherList = refreshedWeather(nextRefreshCount),
                lastUpdated = currentUpdateTime(),
                isRefreshing = false,
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
    val rainOffset = (refreshCount % 2) * 5

    return mockWeatherList.map { weather ->
        weather.copy(
            currentTemp = weather.currentTemp + tempOffset,
            feelsLike = weather.feelsLike + tempOffset,
            precipitationProbability = (weather.precipitationProbability + rainOffset).coerceAtMost(95),
            hourlyForecast = weather.hourlyForecast.mapIndexed { index, hourly ->
                hourly.copy(temperature = hourly.temperature + tempOffset + if (index > 2) -1 else 0)
            }
        )
    }
}

private fun currentUpdateTime(): String =
    LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
