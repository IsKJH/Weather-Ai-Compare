package com.example.weathernow

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
    val isLoading: Boolean = false,
    val lastUpdated: String = currentUpdateTime()
) {
    val selectedWeather: WeatherData
        get() = weatherList[selectedCityIndex]

    val isSelectedCityFavorite: Boolean
        get() = favoriteCityIndex == selectedCityIndex

    val smartInsight: SmartInsight
        get() = computeSmartInsight(selectedWeather)
}

class WeatherViewModel : ViewModel() {
    var uiState by mutableStateOf(WeatherUiState())
        private set

    private var refreshCount by mutableIntStateOf(0)

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
        if (uiState.isLoading) return

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            delay(1_500)
            refreshCount += 1
            uiState = uiState.copy(
                weatherList = refreshedWeather(refreshCount),
                isLoading = false,
                lastUpdated = currentUpdateTime()
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
    val precipOffset = (refreshCount % 2) * 5

    return mockWeatherList.map { weather ->
        weather.copy(
            currentTemp = weather.currentTemp + tempOffset,
            feelsLike = weather.feelsLike + tempOffset,
            precipitationProbability = (weather.precipitationProbability + precipOffset).coerceAtMost(95),
            hourlyForecast = weather.hourlyForecast.mapIndexed { index, hourly ->
                val hourlyOffset = if (index > 2) -1 else 0
                hourly.copy(temperature = hourly.temperature + tempOffset + hourlyOffset)
            }
        )
    }
}

private fun currentUpdateTime(): String =
    LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
