package com.example.weathernow.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Refresh
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
import com.example.weathernow.ui.components.*

@Composable
fun WeatherScreen(viewModel: WeatherViewModel) {
    val weatherData by viewModel.uiState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val cities = listOf("서울", "부산", "제주")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF0D47A1), Color(0xFF1976D2), Color(0xFF42A5F5))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // City Selector & Refresh
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(modifier = Modifier.weight(1f)) {
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
                IconButton(onClick = { viewModel.refreshWeather() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "새로고침", tint = Color.White)
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // City & Favorite
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = weatherData.city,
                            style = MaterialTheme.typography.headlineLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = { viewModel.toggleFavorite() }) {
                            Icon(
                                imageVector = if (weatherData.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "즐겨찾기",
                                tint = if (weatherData.isFavorite) Color.Red else Color.White
                            )
                        }
                    }
                    
                    Text(
                        text = "마지막 업데이트: ${weatherData.lastUpdated}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    WeatherIcon(condition = weatherData.condition, size = 100)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${weatherData.currentTemp}°",
                        fontSize = 90.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Light
                    )
                    Text(
                        text = weatherData.condition,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(40.dp))

                    // Main Details Grid
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            InfoCard(label = "체감온도", value = "${weatherData.feelsLike}°", modifier = Modifier.weight(1f))
                            InfoCard(label = "습도", value = "${weatherData.humidity}%", modifier = Modifier.weight(1f))
                            InfoCard(label = "풍속", value = "${weatherData.windSpeed}m/s", modifier = Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            InfoCard(label = "강수확률", value = "${weatherData.precipitationProbability}%", modifier = Modifier.weight(1f))
                            InfoCard(label = "자외선 지수", value = "${weatherData.uvIndex}", modifier = Modifier.weight(1f))
                            InfoCard(label = "대기질", value = weatherData.airQuality, modifier = Modifier.weight(1f))
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Hourly Forecast
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                            .padding(vertical = 16.dp)
                    ) {
                        Text(
                            text = "시간별 예보",
                            color = Color.White,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(weatherData.hourlyForecast) { hourly ->
                                HourlyForecastItem(hourly = hourly)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 5-day Forecast
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "5일 예보",
                            color = Color.White,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        weatherData.forecast.forEach { forecast ->
                            ForecastItem(forecast = forecast)
                        }
                    }
                }
            }
        }

        // Loading State
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }
    }
}
