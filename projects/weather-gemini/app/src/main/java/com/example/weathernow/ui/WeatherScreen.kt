package com.example.weathernow.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weathernow.WeatherViewModel
import com.example.weathernow.ui.components.ForecastItem
import com.example.weathernow.ui.components.InfoCard
import com.example.weathernow.ui.components.WeatherIcon

@Composable
fun WeatherScreen(viewModel: WeatherViewModel) {
    val weatherData by viewModel.uiState.collectAsState()
    val cities = listOf("서울", "부산", "제주")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF1A237E), Color(0xFF3949AB), Color(0xFF5C6BC0))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // City Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                cities.forEach { city ->
                    val isSelected = weatherData.city == city
                    Text(
                        text = city,
                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.5f),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 18.sp,
                        modifier = Modifier
                            .clickable { viewModel.selectCity(city) }
                            .padding(8.dp)
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        text = weatherData.city,
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    WeatherIcon(condition = weatherData.condition)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "${weatherData.currentTemp}°",
                        fontSize = 80.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Thin
                    )
                    Text(
                        text = weatherData.condition,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(32.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        InfoCard(label = "체감 온도", value = "${weatherData.feelsLike}°", modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(8.dp))
                        InfoCard(label = "습도", value = "${weatherData.humidity}%", modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(8.dp))
                        InfoCard(label = "풍속", value = "${weatherData.windSpeed}m/s", modifier = Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "5일 예보",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        weatherData.forecast.forEach { forecast ->
                            ForecastItem(forecast = forecast)
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
