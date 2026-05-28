package com.example.weathernow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class WeatherViewModel : ViewModel() {
    private val _weatherList = MutableStateFlow(mockWeatherList)
    
    private val _currentCity = MutableStateFlow("서울")
    val currentCity: StateFlow<String> = _currentCity.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _uiState = MutableStateFlow(mockWeatherList[0])
    val uiState: StateFlow<WeatherData> = _uiState.asStateFlow()

    fun selectCity(city: String) {
        _currentCity.value = city
        updateUiState()
    }

    fun refreshWeather() {
        viewModelScope.launch {
            _isLoading.value = true
            delay(1000) // Simulate network delay
            
            val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            _weatherList.value = _weatherList.value.map {
                if (it.city == _currentCity.value) {
                    it.copy(lastUpdated = currentTime)
                } else {
                    it
                }
            }
            updateUiState()
            _isLoading.value = false
        }
    }

    fun toggleFavorite() {
        _weatherList.value = _weatherList.value.map {
            if (it.city == _currentCity.value) {
                it.copy(isFavorite = !it.isFavorite)
            } else {
                // The requirement says "user can mark one city as favorite".
                // Let's interpret this as only one city can be favorite at a time, 
                // or just that they can toggle it. 
                // To keep it simple, let's allow multiple favorites or just toggle the current one.
                // Re-reading: "user can mark one city as favorite" -> implies a single favorite.
                it.copy(isFavorite = false)
            }
        }
        updateUiState()
    }

    private fun updateUiState() {
        val selectedWeather = _weatherList.value.find { it.city == _currentCity.value }
        if (selectedWeather != null) {
            _uiState.value = selectedWeather
        }
    }
}
