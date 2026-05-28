package com.example.weathernow

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class WeatherViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(mockWeatherList[0])
    val uiState: StateFlow<WeatherData> = _uiState.asStateFlow()

    fun selectCity(city: String) {
        val selectedWeather = mockWeatherList.find { it.city == city }
        if (selectedWeather != null) {
            _uiState.value = selectedWeather
        }
    }
}
