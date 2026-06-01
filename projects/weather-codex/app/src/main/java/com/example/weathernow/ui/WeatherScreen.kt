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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
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
import com.example.weathernow.City
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
    error = Color(0xFFFCA5A5),
    onPrimary = Color(0xFF06121E),
    onBackground = Color(0xFFEFF6FF),
    onSurface = Color(0xFFEFF6FF),
    onSurfaceVariant = Color(0xFFB8C4D6)
)

@Composable
fun WeatherNowTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = AppColorScheme, content = content)
}

@Composable
fun WeatherScreen(
    uiState: WeatherUiState,
    onCitySelected: (Int) -> Unit,
    onRefresh: () -> Unit,
    onFavoriteToggle: () -> Unit,
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
                .padding(horizontal = 20.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Header(
                lastUpdated = uiState.lastUpdated,
                isBusy = uiState.isLoading || uiState.isRefreshing,
                onRefresh = onRefresh
            )
            CitySelector(
                cities = uiState.cities,
                selectedIndex = uiState.selectedCityIndex,
                favoriteCityIndex = uiState.favoriteCityIndex,
                onCitySelected = onCitySelected
            )
            uiState.errorMessage?.let { message ->
                ErrorBanner(message = message)
            }

            when {
                uiState.isLoading && weather == null -> LoadingCard()
                weather != null -> {
                    CurrentWeatherCard(
                        weather = weather,
                        isFavorite = uiState.isSelectedCityFavorite,
                        onFavoriteToggle = onFavoriteToggle
                    )
                    CurrentMetrics(weather = weather)
                    SectionTitle(text = "시간별 예보")
                    HourlyForecastStrip(items = weather.hourlyForecast)
                    SectionTitle(text = "5일 예보")
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        weather.forecast.forEach { forecastDay ->
                            ForecastRow(day = forecastDay)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun Header(
    lastUpdated: String,
    isBusy: Boolean,
    onRefresh: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
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
            OutlinedButton(
                onClick = onRefresh,
                enabled = !isBusy,
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f),
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text(text = if (isBusy) "불러오는 중" else "새로고침")
            }
        }
        if (isBusy) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surface
            )
        }
    }
}

@Composable
private fun CitySelector(
    cities: List<City>,
    selectedIndex: Int,
    favoriteCityIndex: Int?,
    onCitySelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        cities.forEachIndexed { index, city ->
            val selected = selectedIndex == index
            OutlinedButton(
                onClick = { onCitySelected(index) },
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 6.dp),
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
                    text = if (favoriteCityIndex == index) "★ ${city.name}" else city.name,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun LoadingCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f)
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            Text(
                text = "실시간 날씨를 불러오는 중입니다.",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ErrorBanner(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.error.copy(alpha = 0.18f)
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(14.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun CurrentWeatherCard(
    weather: WeatherData,
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f)
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = weather.city,
                        style = MaterialTheme.typography.titleLarge,
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
                    onClick = onFavoriteToggle,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isFavorite) {
                            MaterialTheme.colorScheme.secondary
                        } else {
                            Color.Transparent
                        },
                        contentColor = if (isFavorite) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                ) {
                    Text(text = if (isFavorite) "★ 즐겨찾기" else "☆ 즐겨찾기")
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = weather.icon,
                fontSize = 58.sp,
                lineHeight = 62.sp
            )
            Text(
                text = "${weather.currentTemp}°C",
                fontSize = 72.sp,
                lineHeight = 76.sp,
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
private fun CurrentMetrics(weather: WeatherData) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricTile(
                label = "강수확률",
                value = "${weather.precipitationProbability}%",
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                label = "자외선 지수",
                value = weather.uvIndex.toString(),
                modifier = Modifier.weight(1f)
            )
        }
        MetricTile(
            label = "대기질",
            value = weather.airQuality
        )
    }
}

@Composable
private fun HourlyForecastStrip(items: List<HourlyForecast>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items.forEach { item ->
            HourlyForecastCard(item = item)
        }
    }
}

@Composable
private fun HourlyForecastCard(item: HourlyForecast) {
    Card(
        modifier = Modifier.width(86.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.76f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Text(
                text = item.time,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = item.icon,
                fontSize = 28.sp,
                lineHeight = 32.sp
            )
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
