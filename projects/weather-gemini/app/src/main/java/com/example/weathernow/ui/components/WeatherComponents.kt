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
import com.example.weathernow.HourlyForecast

@Composable
fun WeatherIcon(condition: String, modifier: Modifier = Modifier, size: Int = 48) {
    val emoji = when (condition) {
        "맑음" -> "☀️"
        "구름많음" -> "☁️"
        "비" -> "🌧️"
        "천둥번개" -> "⛈️"
        "눈" -> "❄️"
        else -> "❓"
    }
    Text(text = emoji, fontSize = size.sp, modifier = modifier)
}

@Composable
fun InfoCard(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
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
fun HourlyForecastItem(hourly: HourlyForecast) {
    Column(
        modifier = Modifier
            .padding(horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = hourly.time, color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))
        WeatherIcon(condition = hourly.condition, size = 28)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "${hourly.temp}°", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            WeatherIcon(condition = forecast.condition, size = 20)
            Spacer(modifier = Modifier.width(8.dp))
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
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(60.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}
