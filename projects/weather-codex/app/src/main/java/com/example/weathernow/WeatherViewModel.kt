package com.example.weathernow

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

data class WeatherUiState(
    val selectedCityIndex: Int = 0,
    val weatherList: List<WeatherData> = mockWeatherList
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
}
