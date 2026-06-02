package com.example.weathernow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weathernow.data.CITIES
import com.example.weathernow.data.WeatherData
import com.example.weathernow.data.WeatherRepository
import com.example.weathernow.data.db.FavoriteCityDao
import com.example.weathernow.data.db.FavoriteCityEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class WeatherUiState(
    val selectedIndex: Int = 0,
    val weatherData: WeatherData? = null,
    val cities: List<String> = CITIES.map { it.name },
    val isLoading: Boolean = true,
    val error: String? = null,
    val lastUpdated: String = "",
    val favoriteCityIndex: Int? = null
)

private fun currentTimeString(): String =
    LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))

class WeatherViewModel(
    private val favoriteCityDao: FavoriteCityDao? = null
) : ViewModel() {
    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private var fetchJob: Job? = null

    init {
        loadSavedFavorite()
        fetchWeather(0)
    }

    fun selectCity(index: Int) {
        _uiState.value = _uiState.value.copy(selectedIndex = index, error = null)
        fetchWeather(index)
    }

    fun toggleFavorite() {
        val current = _uiState.value
        val newFavorite = if (current.favoriteCityIndex == current.selectedIndex) null
                         else current.selectedIndex
        _uiState.value = current.copy(favoriteCityIndex = newFavorite)
        viewModelScope.launch {
            if (newFavorite != null) {
                favoriteCityDao?.upsert(FavoriteCityEntity(cityIndex = newFavorite))
            } else {
                favoriteCityDao?.clear()
            }
        }
    }

    fun refresh() {
        fetchWeather(_uiState.value.selectedIndex)
    }

    private fun loadSavedFavorite() {
        favoriteCityDao ?: return
        viewModelScope.launch {
            val saved = favoriteCityDao.get()
            if (saved != null) {
                _uiState.value = _uiState.value.copy(favoriteCityIndex = saved.cityIndex)
            }
        }
    }

    private fun fetchWeather(cityIndex: Int) {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val data = WeatherRepository.fetchWeather(CITIES[cityIndex])
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    weatherData = data,
                    lastUpdated = currentTimeString(),
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "날씨 데이터를 불러오지 못했습니다: ${e.message}"
                )
            }
        }
    }
}
