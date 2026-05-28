package com.example.weathernow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.weathernow.ui.WeatherNowTheme
import com.example.weathernow.ui.WeatherScreen

class MainActivity : ComponentActivity() {
    private val viewModel: WeatherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WeatherNowTheme {
                WeatherScreen(
                    uiState = viewModel.uiState,
                    onCitySelected = viewModel::selectCity,
                    onRefresh = viewModel::refresh,
                    onToggleFavorite = viewModel::toggleFavorite
                )
            }
        }
    }
}
