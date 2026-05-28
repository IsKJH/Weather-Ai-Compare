package com.example.weathernow.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weathernow.ForecastDay
import com.example.weathernow.HourlyForecast

@Composable
fun WeatherIcon(condition: String, fontSize: Int = 48, modifier: Modifier = Modifier) {
    val emoji = when (condition) {
        "맑음" -> "☀️"
        "구름많음" -> "☁️"
        "비" -> "🌧️"
        "천둥번개" -> "⛈️"
        "눈" -> "❄️"
        else -> "❓"
    }
    Text(text = emoji, fontSize = fontSize.sp, modifier = modifier)
}

@Composable
fun InfoCard(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label, 
            style = MaterialTheme.typography.labelSmall, 
            color = Color.White.copy(alpha = 0.7f),
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value, 
            style = MaterialTheme.typography.titleMedium, 
            color = Color.White, 
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ForecastItem(forecast: ForecastDay) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = forecast.day, 
            color = Color.White, 
            modifier = Modifier.width(40.dp),
            fontWeight = FontWeight.Medium
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.Start
        ) {
            WeatherIcon(condition = forecast.condition, fontSize = 24)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = forecast.condition, 
                color = Color.White.copy(alpha = 0.9f), 
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Text(
            text = "${forecast.high}° / ${forecast.low}°",
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun HourlyForecastItem(hourly: HourlyForecast) {
    Column(
        modifier = Modifier
            .padding(horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = hourly.time, 
            color = Color.White.copy(alpha = 0.8f), 
            style = MaterialTheme.typography.labelSmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        WeatherIcon(condition = hourly.condition, fontSize = 28)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "${hourly.temp}°", 
            color = Color.White, 
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun LoadingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = Color.White)
    }
}
