package com.example.weathernow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.weathernow.data.RoomFavoriteCityRepository
import com.example.weathernow.data.WeatherNowDatabase
import com.example.weathernow.ui.WeatherNowTheme
import com.example.weathernow.ui.WeatherScreen

class MainActivity : ComponentActivity() {
    private val viewModel: WeatherViewModel by viewModels {
        val database = WeatherNowDatabase.getInstance(applicationContext)
        WeatherViewModelFactory(
            RoomFavoriteCityRepository(database.favoriteCityDao())
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WeatherNowTheme {
                WeatherScreen(
                    uiState = viewModel.uiState,
                    onCitySelected = viewModel::selectCity,
                    onRefresh = viewModel::refresh,
                    onFavoriteToggle = viewModel::toggleFavorite
                )
            }
        }
    }
}

private class WeatherViewModelFactory(
    private val favoriteCityRepository: RoomFavoriteCityRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WeatherViewModel::class.java)) {
            return WeatherViewModel(favoriteCityRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
