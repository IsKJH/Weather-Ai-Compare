package com.example.weathernow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.weathernow.data.db.FavoriteCityDao

class WeatherViewModelFactory(
    private val favoriteCityDao: FavoriteCityDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return WeatherViewModel(favoriteCityDao) as T
    }
}
