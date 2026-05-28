package com.example.weathernow.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
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
    "구름많음", "구름" -> "⛅"
    "흐림" -> "☁️"
    "비" -> "🌧️"
    "눈" -> "❄️"
    "뇌우" -> "⛈️"
    else -> "🌤️"
}

fun conditionGradient(condition: String): List<Color> = when (condition) {
    "맑음" -> listOf(Color(0xFF1565C0), Color(0xFF42A5F5))
    "구름많음", "구름" -> listOf(Color(0xFF546E7A), Color(0xFF90A4AE))
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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(gradient))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            CityTabRow(
                cities = uiState.cities,
                selectedIndex = uiState.selectedIndex,
                favoriteIndex = uiState.favoriteIndex,
                onSelect = viewModel::selectCity
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = weather.city,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { viewModel.toggleFavorite() }
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (uiState.favoriteIndex == uiState.selectedIndex) "★" else "☆",
                        fontSize = 22.sp,
                        color = if (uiState.favoriteIndex == uiState.selectedIndex)
                            Color(0xFFFFD700) else Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = conditionEmoji(weather.condition),
                fontSize = 72.sp
            )

            Text(
                text = "${weather.currentTemp}°C",
                fontSize = 68.sp,
                fontWeight = FontWeight.Thin,
                color = Color.White
            )

            Text(
                text = weather.condition,
                fontSize = 20.sp,
                color = Color.White.copy(alpha = 0.9f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            RefreshRow(
                lastUpdated = uiState.lastUpdated,
                isLoading = uiState.isLoading,
                onRefresh = viewModel::refresh
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DetailCard(modifier = Modifier.weight(1f), label = "체감온도", value = "${weather.feelsLike}°C", icon = "🌡️")
                DetailCard(modifier = Modifier.weight(1f), label = "습도", value = "${weather.humidity}%", icon = "💧")
                DetailCard(modifier = Modifier.weight(1f), label = "풍속", value = "${weather.windSpeed}m/s", icon = "💨")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DetailCard(modifier = Modifier.weight(1f), label = "강수확률", value = "${weather.precipitationProbability}%", icon = "🌂")
                DetailCard(modifier = Modifier.weight(1f), label = "자외선 지수", value = "${weather.uvIndex}", icon = "🌞")
                DetailCard(modifier = Modifier.weight(1f), label = "대기질", value = weather.airQuality, icon = "🍃")
            }

            Spacer(modifier = Modifier.height(16.dp))

            HourlyForecastCard(hourlyForecast = weather.hourlyForecast)

            Spacer(modifier = Modifier.height(16.dp))

            ForecastCard(forecast = weather.forecast)

            Spacer(modifier = Modifier.height(32.dp))
        }

        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 3.dp)
            }
        }
    }
}

@Composable
fun CityTabRow(
    cities: List<String>,
    selectedIndex: Int,
    favoriteIndex: Int?,
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
            val isFav = index == favoriteIndex
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) Color.White.copy(alpha = 0.9f) else Color.Transparent)
                    .clickable { onSelect(index) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    if (isFav) {
                        Text(text = "★", fontSize = 10.sp, color = Color(0xFFFFD700))
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
fun RefreshRow(
    lastUpdated: String,
    isLoading: Boolean,
    onRefresh: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "마지막 업데이트: $lastUpdated",
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.7f)
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.2f))
                .clickable(enabled = !isLoading) { onRefresh() }
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(text = "🔄 새로고침", fontSize = 12.sp, color = Color.White)
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
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = icon, fontSize = 22.sp)
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = Color.White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            lineHeight = 13.sp
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
        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = Color.White.copy(alpha = 0.3f))
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            hourlyForecast.forEach { item ->
                HourlyItem(item = item)
            }
        }
    }
}

@Composable
fun HourlyItem(item: HourlyForecast) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
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
            text = "${item.temp}°",
            fontSize = 14.sp,
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
