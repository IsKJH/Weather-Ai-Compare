package com.example.weathernow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weathernow.data.WeatherData
import com.example.weathernow.data.mockWeatherList
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class WeatherUiState(
    val selectedIndex: Int = 0,
    val weatherData: WeatherData = mockWeatherList[0],
    val cities: List<String> = mockWeatherList.map { it.city },
    val isLoading: Boolean = false,
    val lastUpdated: String = "",
    val favoriteCityIndex: Int? = null
)

private fun currentTimeString(): String =
    LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))

class WeatherViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(
        WeatherUiState(lastUpdated = currentTimeString())
    )
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    fun selectCity(index: Int) {
        _uiState.value = _uiState.value.copy(
            selectedIndex = index,
            weatherData = mockWeatherList[index]
        )
    }

    fun toggleFavorite() {
        val current = _uiState.value
        val newFavorite = if (current.favoriteCityIndex == current.selectedIndex) null
                         else current.selectedIndex
        _uiState.value = current.copy(favoriteCityIndex = newFavorite)
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            delay(1200)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                lastUpdated = currentTimeString()
            )
        }
    }
}
