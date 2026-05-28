package com.example.weathernow

data class WeatherData(
    val city: String,
    val currentTemp: Int,
    val feelsLike: Int,
    val condition: String,
    val humidity: Int,
    val windSpeed: Double,
    val forecast: List<ForecastDay>
)

data class ForecastDay(
    val day: String,
    val condition: String,
    val high: Int,
    val low: Int
)

val mockWeatherList = listOf(
    WeatherData(
        city = "??",
        currentTemp = 23,
        feelsLike = 21,
        condition = "??",
        humidity = 55,
        windSpeed = 3.2,
        forecast = listOf(
            ForecastDay("?", "??", 25, 16),
            ForecastDay("?", "????", 22, 15),
            ForecastDay("?", "?", 18, 13),
            ForecastDay("?", "??", 20, 14),
            ForecastDay("?", "??", 26, 17)
        )
    ),
    WeatherData(
        city = "??",
        currentTemp = 26,
        feelsLike = 28,
        condition = "????",
        humidity = 72,
        windSpeed = 5.1,
        forecast = listOf(
            ForecastDay("?", "????", 27, 20),
            ForecastDay("?", "?", 24, 19),
            ForecastDay("?", "?", 22, 18),
            ForecastDay("?", "??", 25, 19),
            ForecastDay("?", "??", 28, 21)
        )
    ),
    WeatherData(
        city = "??",
        currentTemp = 28,
        feelsLike = 30,
        condition = "??",
        humidity = 68,
        windSpeed = 6.8,
        forecast = listOf(
            ForecastDay("?", "??", 29, 22),
            ForecastDay("?", "??", 30, 23),
            ForecastDay("?", "????", 27, 21),
            ForecastDay("?", "?", 24, 20),
            ForecastDay("?", "??", 25, 21)
        )
    )
)
