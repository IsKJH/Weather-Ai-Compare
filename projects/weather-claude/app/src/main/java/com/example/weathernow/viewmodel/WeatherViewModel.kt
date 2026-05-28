package com.example.weathernow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weathernow.data.WeatherData
import com.example.weathernow.data.mockWeatherList
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class WeatherUiState(
    val selectedIndex: Int = 0,
    val weatherData: WeatherData = mockWeatherList[0],
    val cities: List<String> = mockWeatherList.map { it.city },
    val isLoading: Boolean = false,
    val lastUpdated: String = currentTimeString(),
    val favoriteIndex: Int? = null
)

fun currentTimeString(): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.KOREA)
    return sdf.format(Date())
}

class WeatherViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    fun selectCity(index: Int) {
        _uiState.update { it.copy(
            selectedIndex = index,
            weatherData = mockWeatherList[index]
        )}
    }

    fun toggleFavorite() {
        _uiState.update { current ->
            val newFavorite = if (current.favoriteIndex == current.selectedIndex) null else current.selectedIndex
            current.copy(favoriteIndex = newFavorite)
        }
    }

    fun refresh() {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            delay(1200L)
            val index = _uiState.value.selectedIndex
            _uiState.update { it.copy(
                isLoading = false,
                lastUpdated = currentTimeString(),
                weatherData = mockWeatherList[index]
            )}
        }
    }
}
