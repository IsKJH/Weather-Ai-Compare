package com.example.weathernow.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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

    val backgroundColor1 by animateColorAsState(
        targetValue = when {
            weatherData == null -> Color(0xFF1A237E)
            weatherData.currentTemp > 27 -> Color(0xFFE64A19)
            weatherData.currentTemp < 20 -> Color(0xFF0288D1)
            else -> Color(0xFF1A237E)
        },
        animationSpec = tween(durationMillis = 1000), label = ""
    )
    val backgroundColor2 by animateColorAsState(
        targetValue = when {
            weatherData == null -> Color(0xFF3F51B5)
            weatherData.currentTemp > 27 -> Color(0xFFFBC02D)
            weatherData.currentTemp < 20 -> Color(0xFF26C6DA)
            else -> Color(0xFF3F51B5)
        },
        animationSpec = tween(durationMillis = 1000), label = ""
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(backgroundColor1, backgroundColor2)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    cities.forEach { city ->
                        val isSelected = weatherData?.city == city
                        Text(
                            text = city,
                            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.5f),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 18.sp,
                            modifier = Modifier.clickable { viewModel.selectCity(city) }
                        )
                    }
                }
                IconButton(onClick = { viewModel.refresh() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.White)
                }
            }

            if (uiState.error != null) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Error, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = uiState.error!!, color = Color.White, style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = { viewModel.refresh() }) {
                        Text("다시 시도")
                    }
                }
            } else if (weatherData != null) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = uiState.smartInsight,
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    item {
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
                                        contentDescription = "Favorite",
                                        tint = if (uiState.favoriteCity == weatherData.city) Color.Red else Color.White
                                    )
                                }
                            }
                            Text(
                                text = "업데이트: ${uiState.lastUpdated}",
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
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                                .padding(16.dp)
                        ) {
                            Text("시간별 예보", color = Color.White, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(16.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(weatherData.hourlyForecast) { item -> HourlyForecastItem(item) }
                            }
                        }
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                InfoCard(label = "체감", value = "${weatherData.feelsLike}°", modifier = Modifier.weight(1f))
                                InfoCard(label = "습도", value = "${weatherData.humidity}%", modifier = Modifier.weight(1f))
                                InfoCard(label = "풍속", value = "${weatherData.windSpeed}m/s", modifier = Modifier.weight(1f))
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                InfoCard(label = "강수", value = "${weatherData.precipitation}%", modifier = Modifier.weight(1f))
                                InfoCard(label = "자외선", value = "${weatherData.uvIndex}", modifier = Modifier.weight(1f))
                                InfoCard(label = "미세먼지", value = weatherData.airQuality, modifier = Modifier.weight(1f))
                            }
                        }
                    }

                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                                .padding(16.dp)
                        ) {
                            Text("5일간의 예보", color = Color.White, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            weatherData.forecast.forEach { forecast -> ForecastItem(forecast) }
                        }
                    }
                }
            }
        }
        if (uiState.isLoading) LoadingOverlay()
    }
}
