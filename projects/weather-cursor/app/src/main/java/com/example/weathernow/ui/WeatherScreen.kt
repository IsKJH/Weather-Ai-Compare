package com.example.weathernow.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weathernow.WeatherUiState
import com.example.weathernow.backgroundColorsFor
import com.example.weathernow.conditionEmoji
import com.example.weathernow.ui.components.CityTabBar
import com.example.weathernow.ui.components.DetailChip
import com.example.weathernow.ui.components.ForecastCard
import com.example.weathernow.ui.components.HourlyCard
import com.example.weathernow.ui.components.SmartInsightCard
import com.example.weathernow.ui.components.WeatherHeader

private val CursorTeal = Color(0xFF0E7490)
private val CursorTealLight = Color(0xFFECFEFF)
private val CursorSky = Color(0xFFE0F2FE)

private val WeatherColorScheme = lightColorScheme(
    primary = CursorTeal,
    onPrimary = Color.White,
    background = CursorTealLight,
    surface = Color.White,
    onBackground = Color(0xFF164E63),
    onSurface = Color(0xFF164E63),
    onSurfaceVariant = Color(0xFF64748B),
    surfaceVariant = CursorSky
)

@Composable
fun WeatherNowTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = WeatherColorScheme, content = content)
}

@Composable
fun WeatherScreen(
    uiState: WeatherUiState,
    onCitySelected: (Int) -> Unit,
    onFavoriteToggle: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val weather = uiState.selectedWeather
    val insight = uiState.smartInsight
    val cities = uiState.weatherList.map { it.city }
    val bgColors = backgroundColorsFor(weather.currentTemp)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = bgColors))
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            WeatherHeader(
                lastUpdated = uiState.lastUpdated,
                onRefresh = onRefresh,
                accent = CursorTeal
            )

            Spacer(modifier = Modifier.height(16.dp))

            CityTabBar(
                cities = cities,
                selectedIndex = uiState.selectedCityIndex,
                favoriteCityIndex = uiState.favoriteCityIndex,
                onCitySelected = onCitySelected,
                onFavoriteToggle = onFavoriteToggle
            )

            Text(
                text = if (uiState.favoriteCityIndex != null) {
                    "즐겨찾기: ${cities[uiState.favoriteCityIndex]} · 탭을 다시 눌러 해제"
                } else {
                    "선택한 도시 탭을 다시 누르면 즐겨찾기 ⭐"
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            CurrentWeatherCard(weather = weather)

            Spacer(modifier = Modifier.height(16.dp))

            SmartInsightCard(insight = insight, accent = CursorTeal)

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DetailChip(label = "체감온도", value = "${weather.feelsLike}°C", modifier = Modifier.weight(1f))
                DetailChip(label = "습도", value = "${weather.humidity}%", modifier = Modifier.weight(1f))
                DetailChip(label = "풍속", value = "${weather.windSpeed} m/s", modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DetailChip(label = "강수확률", value = "${weather.precipitationProbability}%", modifier = Modifier.weight(1f))
                DetailChip(label = "자외선 지수", value = "${weather.uvIndex}", modifier = Modifier.weight(1f))
                DetailChip(label = "대기질", value = weather.airQuality, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(24.dp))

            SectionTitle("시간별 예보")

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                weather.hourlyForecast.forEach { hourly ->
                    HourlyCard(hourly = hourly)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            SectionTitle("5일 예보")

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                weather.forecast.forEach { day ->
                    ForecastCard(day = day)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.65f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = CursorTeal)
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
private fun CurrentWeatherCard(weather: com.example.weathernow.WeatherData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = weather.city,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = conditionEmoji(weather.condition),
                fontSize = 64.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Text(
                text = "${weather.currentTemp}°",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Light,
                    fontSize = 72.sp
                ),
                color = CursorTeal
            )
            Text(
                text = weather.condition,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
