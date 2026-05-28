package com.example.weathernow.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.weathernow.data.ForecastDay
import com.example.weathernow.data.HourlyForecast
import com.example.weathernow.viewmodel.WeatherViewModel

fun conditionEmoji(condition: String): String = when (condition) {
    "맑음" -> "☀️"
    "구름" -> "⛅"
    "흐림" -> "☁️"
    "비" -> "🌧️"
    "눈" -> "❄️"
    "뇌우" -> "⛈️"
    else -> "🌤️"
}

fun conditionGradient(condition: String): List<Color> = when (condition) {
    "맑음" -> listOf(Color(0xFF1565C0), Color(0xFF42A5F5))
    "구름" -> listOf(Color(0xFF546E7A), Color(0xFF90A4AE))
    "흐림" -> listOf(Color(0xFF37474F), Color(0xFF78909C))
    "비" -> listOf(Color(0xFF1A237E), Color(0xFF5C6BC0))
    "눈" -> listOf(Color(0xFF4DD0E1), Color(0xFFE0F7FA))
    "뇌우" -> listOf(Color(0xFF212121), Color(0xFF424242))
    else -> listOf(Color(0xFF1565C0), Color(0xFF42A5F5))
}

@Composable
fun WeatherScreen(viewModel: WeatherViewModel, modifier: Modifier = Modifier) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val weather = uiState.weatherData
    val gradient = conditionGradient(weather.condition)
    val isFavorite = uiState.favoriteCityIndex == uiState.selectedIndex

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(gradient))
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color.White,
                strokeWidth = 3.dp
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                CityTabRow(
                    cities = uiState.cities,
                    selectedIndex = uiState.selectedIndex,
                    favoriteCityIndex = uiState.favoriteCityIndex,
                    onSelect = viewModel::selectCity
                )

                Spacer(modifier = Modifier.height(12.dp))

                LastUpdatedRow(
                    lastUpdated = uiState.lastUpdated,
                    onRefresh = viewModel::refresh
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = weather.city,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = if (isFavorite) "⭐" else "☆",
                        fontSize = 22.sp,
                        modifier = Modifier.clickable { viewModel.toggleFavorite() }
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = conditionEmoji(weather.condition),
                    fontSize = 64.sp
                )

                Text(
                    text = "${weather.currentTemp}°C",
                    fontSize = 68.sp,
                    fontWeight = FontWeight.Thin,
                    color = Color.White
                )

                Text(
                    text = weather.condition,
                    fontSize = 18.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )

                Spacer(modifier = Modifier.height(28.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DetailCard(Modifier.weight(1f), "체감온도", "${weather.feelsLike}°C", "🌡️")
                    DetailCard(Modifier.weight(1f), "습도", "${weather.humidity}%", "💧")
                    DetailCard(Modifier.weight(1f), "풍속", "${weather.windSpeed}m/s", "💨")
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DetailCard(Modifier.weight(1f), "강수확률", "${weather.precipitationProbability}%", "🌂")
                    DetailCard(Modifier.weight(1f), "자외선 지수", "${weather.uvIndex}", "🔆")
                    DetailCard(Modifier.weight(1f), "대기질", weather.airQuality, "🌿")
                }

                Spacer(modifier = Modifier.height(20.dp))

                HourlyForecastCard(hourlyForecast = weather.hourlyForecast)

                Spacer(modifier = Modifier.height(16.dp))

                ForecastCard(forecast = weather.forecast)

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun LastUpdatedRow(lastUpdated: String, onRefresh: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (lastUpdated.isNotEmpty()) {
            Text(
                text = "마지막 업데이트: $lastUpdated",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = "🔄",
            fontSize = 20.sp,
            modifier = Modifier.clickable { onRefresh() }
        )
    }
}

@Composable
fun CityTabRow(
    cities: List<String>,
    selectedIndex: Int,
    favoriteCityIndex: Int?,
    onSelect: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.2f))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        cities.forEachIndexed { index, city ->
            val isSelected = index == selectedIndex
            val isFav = index == favoriteCityIndex
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (isSelected) Color.White.copy(alpha = 0.9f)
                        else Color.Transparent
                    )
                    .clickable { onSelect(index) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isFav) {
                        Text(text = "⭐", fontSize = 10.sp)
                        Spacer(Modifier.width(2.dp))
                    }
                    Text(
                        text = city,
                        color = if (isSelected) Color(0xFF1565C0) else Color.White,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
fun DetailCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: String
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.2f))
            .padding(vertical = 14.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(text = icon, fontSize = 22.sp)
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = Color.White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun HourlyForecastCard(hourlyForecast: List<HourlyForecast>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.2f))
            .padding(16.dp)
    ) {
        Text(
            text = "시간별 예보",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.8f)
        )
        Spacer(Modifier.height(8.dp))
        HorizontalDivider(color = Color.White.copy(alpha = 0.3f))
        Spacer(Modifier.height(12.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            items(hourlyForecast) { item ->
                HourlyItem(item = item)
            }
        }
    }
}

@Composable
fun HourlyItem(item: HourlyForecast) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = item.time,
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.8f)
        )
        Text(
            text = conditionEmoji(item.condition),
            fontSize = 22.sp
        )
        Text(
            text = "${item.temperature}°",
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
    }
}

@Composable
fun ForecastCard(forecast: List<ForecastDay>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.2f))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "5일 예보",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.8f)
        )
        HorizontalDivider(color = Color.White.copy(alpha = 0.3f))
        forecast.forEach { day ->
            ForecastRow(day = day)
        }
    }
}

@Composable
fun ForecastRow(day: ForecastDay) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = day.day,
            modifier = Modifier.width(36.dp),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
        Text(
            text = conditionEmoji(day.condition),
            modifier = Modifier.width(40.dp),
            fontSize = 22.sp,
            textAlign = TextAlign.Center
        )
        Text(
            text = day.condition,
            modifier = Modifier.weight(1f),
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.85f)
        )
        Text(
            text = "${day.high}° / ${day.low}°",
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
    }
}
