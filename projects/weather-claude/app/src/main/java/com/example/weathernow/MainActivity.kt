package com.example.weathernow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weathernow.data.db.AppDatabase
import com.example.weathernow.ui.WeatherScreen
import com.example.weathernow.ui.theme.WeatherNowTheme
import com.example.weathernow.viewmodel.WeatherViewModel
import com.example.weathernow.viewmodel.WeatherViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val dao = AppDatabase.getInstance(applicationContext).favoriteCityDao()
        setContent {
            WeatherNowTheme {
                val viewModel: WeatherViewModel = viewModel(
                    factory = WeatherViewModelFactory(dao)
                )
                WeatherScreen(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
