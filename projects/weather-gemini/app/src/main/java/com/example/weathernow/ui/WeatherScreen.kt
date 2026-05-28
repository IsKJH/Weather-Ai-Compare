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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.FavoriteBorder
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
    val uiState by viewModel.uiState.collectAsState()
    val weatherData = uiState.weatherData
    val cities = listOf("서울", "부산", "제주")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF1A237E), Color(0xFF303F9F), Color(0xFF3F51B5))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            // Top Bar: City Selector & Refresh
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    cities.forEach { city ->
                        val isSelected = weatherData.city == city
                        Text(
                            text = city,
                            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.5f),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 18.sp,
                            modifier = Modifier
                                .clickable { viewModel.selectCity(city) }
                        )
                    }
                }
                IconButton(onClick = { viewModel.refresh() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "새로고침", tint = Color.White)
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    // Current Weather Section
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = weatherData.city,
                                style = MaterialTheme.typography.headlineLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = { viewModel.toggleFavorite(weatherData.city) }) {
                                Icon(
                                    imageVector = if (uiState.favoriteCity == weatherData.city) 
                                        Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                    contentDescription = "즐겨찾기",
                                    tint = if (uiState.favoriteCity == weatherData.city) Color.Red else Color.White
                                )
                            }
                        }
                        
                        Text(
                            text = "마지막 업데이트: ${uiState.lastUpdated}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        WeatherIcon(condition = weatherData.condition, fontSize = 100)
                        
                        Text(
                            text = "${weatherData.currentTemp}°",
                            fontSize = 84.sp,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraLight
                        )
                        Text(
                            text = weatherData.condition,
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }

                item {
                    // Hourly Forecast Section
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "시간별 예보",
                            color = Color.White,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(weatherData.hourlyForecast) { hourly ->
                                HourlyForecastItem(hourly = hourly)
                            }
                        }
                    }
                }

                item {
                    // Weather Details Grid
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            InfoCard(label = "체감 온도", value = "${weatherData.feelsLike}°", modifier = Modifier.weight(1f))
                            InfoCard(label = "습도", value = "${weatherData.humidity}%", modifier = Modifier.weight(1f))
                            InfoCard(label = "풍속", value = "${weatherData.windSpeed}m/s", modifier = Modifier.weight(1f))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            InfoCard(label = "강수확률", value = "${weatherData.precipitation}%", modifier = Modifier.weight(1f))
                            InfoCard(label = "자외선 지수", value = "${weatherData.uvIndex}", modifier = Modifier.weight(1f))
                            InfoCard(label = "대기질", value = weatherData.airQuality, modifier = Modifier.weight(1f))
                        }
                    }
                }

                item {
                    // 5-Day Forecast Section
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
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

        if (uiState.isLoading) {
            LoadingOverlay()
        }
    }
}
