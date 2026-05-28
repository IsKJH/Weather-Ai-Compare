package com.example.weathernow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class WeatherUiState(
    val weatherData: WeatherData,
    val isLoading: Boolean = false,
    val favoriteCity: String? = null,
    val lastUpdated: String = ""
)

class WeatherViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(
        WeatherUiState(
            weatherData = mockWeatherList[0],
            lastUpdated = getCurrentTime()
        )
    )
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    fun selectCity(city: String) {
        val selectedWeather = mockWeatherList.find { it.city == city }
        if (selectedWeather != null) {
            _uiState.update { it.copy(weatherData = selectedWeather) }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            delay(1000) // Simulate loading
            _uiState.update { 
                it.copy(
                    isLoading = false,
                    lastUpdated = getCurrentTime()
                )
            }
        }
    }

    fun toggleFavorite(city: String) {
        _uiState.update { 
            val newFavorite = if (it.favoriteCity == city) null else city
            it.copy(favoriteCity = newFavorite)
        }
    }

    private fun getCurrentTime(): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.KOREA)
        return sdf.format(Date())
    }
}
