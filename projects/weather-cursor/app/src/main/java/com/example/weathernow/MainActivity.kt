package com.example.weathernow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weathernow.ui.WeatherNowTheme
import com.example.weathernow.ui.WeatherScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: WeatherViewModel = viewModel()
            WeatherNowTheme {
                WeatherScreen(
                    uiState = viewModel.uiState,
                    onCitySelected = viewModel::selectCity,
                    onFavoriteToggle = viewModel::toggleFavorite,
                    onRefresh = viewModel::refresh
                )
            }
        }
    }
}
