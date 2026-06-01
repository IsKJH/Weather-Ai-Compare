package com.example.weathernow.data

data class WeatherData(
    val city: String,
    val currentTemp: Int,
    val feelsLike: Int,
    val condition: String,
    val humidity: Int,
    val windSpeed: Double,
    val forecast: List<ForecastDay>,
    val hourlyForecast: List<HourlyForecast>,
    val precipitationProbability: Int,
    val uvIndex: Int,
    val airQuality: String
)

data class ForecastDay(
    val day: String,
    val condition: String,
    val high: Int,
    val low: Int
)

data class HourlyForecast(
    val time: String,
    val condition: String,
    val temperature: Int
)

data class CityInfo(val name: String, val lat: Double, val lon: Double)

val CITIES = listOf(
    CityInfo("서울", 37.5665, 126.9780),
    CityInfo("부산", 35.1796, 129.0756),
    CityInfo("제주", 33.4996, 126.5312)
)

fun wmoToCondition(code: Int): String = when {
    code == 0 -> "맑음"
    code == 1 -> "구름조금"
    code == 2 -> "구름많음"
    code == 3 -> "흐림"
    code in 45..48 -> "안개"
    code in 51..55 -> "이슬비"
    code in 61..65 -> "비"
    code in 71..75 -> "눈"
    code in 80..82 -> "소나기"
    code in 95..99 -> "뇌우"
    else -> "맑음"
}
