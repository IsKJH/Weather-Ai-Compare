package com.example.weathernow.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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

@Composable
fun WeatherIcon(condition: String, modifier: Modifier = Modifier) {
    val emoji = when (condition) {
        "맑음" -> "☀️"
        "구름많음" -> "☁️"
        "비" -> "🌧️"
        "천둥번개" -> "⛈️"
        "눈" -> "❄️"
        else -> "❓"
    }
    Text(text = emoji, fontSize = 48.sp, modifier = modifier)
}

@Composable
fun InfoCard(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.8f))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ForecastItem(forecast: ForecastDay) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = forecast.day, color = Color.White, modifier = Modifier.width(40.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            val emoji = when (forecast.condition) {
                "맑음" -> "☀️"
                "구름많음" -> "☁️"
                "비" -> "🌧️"
                else -> "❓"
            }
            Text(text = emoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = forecast.condition, color = Color.White, style = MaterialTheme.typography.bodyMedium)
        }
        Text(
            text = "${forecast.high}° / ${forecast.low}°",
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}
