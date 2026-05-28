package com.example.weathernow.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weathernow.HourlyForecast
import com.example.weathernow.WeatherData
import com.example.weathernow.WeatherUiState
import com.example.weathernow.ui.components.ForecastRow
import com.example.weathernow.ui.components.MetricTile

private val AppColorScheme = darkColorScheme(
    primary = Color(0xFF7DD3FC),
    secondary = Color(0xFFFCD34D),
    background = Color(0xFF0D1321),
    surface = Color(0xFF172033),
    onPrimary = Color(0xFF06121E),
    onBackground = Color(0xFFEFF6FF),
    onSurface = Color(0xFFEFF6FF),
    onSurfaceVariant = Color(0xFFB8C4D6)
)

private val cityLabels = listOf("서울", "부산", "제주")

@Composable
fun WeatherNowTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = AppColorScheme, content = content)
}

@Composable
fun WeatherScreen(
    uiState: WeatherUiState,
    onCitySelected: (Int) -> Unit,
    onRefresh: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val weather = uiState.selectedWeather

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0D1321),
                        Color(0xFF12304C),
                        Color(0xFF0D1321)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Header(
                lastUpdated = uiState.lastUpdated,
                isRefreshing = uiState.isRefreshing,
                onRefresh = onRefresh
            )
            CitySelector(
                selectedIndex = uiState.selectedCityIndex,
                onCitySelected = onCitySelected
            )
            CurrentWeatherCard(
                weather = weather,
                isFavorite = uiState.favoriteCityIndex == uiState.selectedCityIndex,
                onToggleFavorite = onToggleFavorite
            )
            PrimaryMetrics(weather)
            SectionTitle("시간별 예보")
            HourlyForecastStrip(weather.hourlyForecast)
            SectionTitle("상세 날씨")
            ExtraDetails(weather)
            SectionTitle("5일 예보")
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                weather.forecast.forEach { forecastDay ->
                    ForecastRow(day = forecastDay)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun Header(
    lastUpdated: String,
    isRefreshing: Boolean,
    onRefresh: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "WeatherNow",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "마지막 업데이트 $lastUpdated",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Button(
            onClick = onRefresh,
            enabled = !isRefreshing,
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
        ) {
            if (isRefreshing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("새로고침")
            }
        }
    }
}

@Composable
private fun CitySelector(
    selectedIndex: Int,
    onCitySelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        cityLabels.forEachIndexed { index, label ->
            val selected = selectedIndex == index
            OutlinedButton(
                onClick = { onCitySelected(index) },
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (selected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.42f)
                    },
                    contentColor = if (selected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
            ) {
                Text(
                    text = label,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun CurrentWeatherCard(
    weather: WeatherData,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f)
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = weather.city,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = weather.condition,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                OutlinedButton(
                    onClick = onToggleFavorite,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Text(if (isFavorite) "★ 즐겨찾기" else "☆ 즐겨찾기")
                }
            }
            Text(
                text = conditionIcon(weather.condition),
                fontSize = 54.sp,
                lineHeight = 58.sp
            )
            Text(
                text = "${weather.currentTemp}°C",
                fontSize = 68.sp,
                lineHeight = 72.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "체감온도 ${weather.feelsLike}°C",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PrimaryMetrics(weather: WeatherData) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MetricTile(
            label = "습도",
            value = "${weather.humidity}%",
            modifier = Modifier.weight(1f)
        )
        MetricTile(
            label = "풍속",
            value = "${weather.windSpeed} m/s",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ExtraDetails(weather: WeatherData) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricTile("강수확률", "${weather.precipitationProbability}%", Modifier.weight(1f))
            MetricTile("자외선 지수", weather.uvIndex.toString(), Modifier.weight(1f))
        }
        MetricTile("대기질", weather.airQuality)
    }
}

@Composable
private fun HourlyForecastStrip(hourlyForecast: List<HourlyForecast>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        hourlyForecast.forEach { item ->
            HourlyForecastItem(item)
        }
    }
}

@Composable
private fun HourlyForecastItem(item: HourlyForecast) {
    Card(
        modifier = Modifier.width(82.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.76f)
        )
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = item.time,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            Text(text = item.condition, fontSize = 26.sp, lineHeight = 30.sp)
            Text(
                text = "${item.temperature}°",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
    )
}

private fun conditionIcon(condition: String): String =
    when (condition) {
        "맑음" -> "☀️"
        "구름많음" -> "☁️"
        "비" -> "🌧️"
        else -> "🌤️"
    }
