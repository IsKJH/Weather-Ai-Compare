package com.example.weathernow.viewmodel

import androidx.lifecycle.ViewModel
import com.example.weathernow.data.WeatherData
import com.example.weathernow.data.mockWeatherList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class WeatherUiState(
    val selectedIndex: Int = 0,
    val weatherData: WeatherData = mockWeatherList[0],
    val cities: List<String> = mockWeatherList.map { it.city }
)

class WeatherViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    fun selectCity(index: Int) {
        _uiState.value = WeatherUiState(
            selectedIndex = index,
            weatherData = mockWeatherList[index],
            cities = mockWeatherList.map { it.city }
        )
    }
}
