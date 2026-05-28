package com.example.weathernow

data class WeatherData(
    val city: String,
    val currentTemp: Int,
    val feelsLike: Int,
    val condition: String,
    val humidity: Int,
    val windSpeed: Double,
    val precipitationProbability: Int,
    val uvIndex: Int,
    val airQuality: String,
    val hourlyForecast: List<HourlyForecast>,
    val forecast: List<ForecastDay>
)

data class HourlyForecast(
    val time: String,
    val condition: String,
    val temperature: Int
)

data class ForecastDay(
    val day: String,
    val condition: String,
    val high: Int,
    val low: Int
)

val mockWeatherList = listOf(
    WeatherData(
        city = "서울",
        currentTemp = 23,
        feelsLike = 21,
        condition = "맑음",
        humidity = 55,
        windSpeed = 3.2,
        precipitationProbability = 10,
        uvIndex = 6,
        airQuality = "보통",
        hourlyForecast = listOf(
            HourlyForecast("지금", "☀️", 23),
            HourlyForecast("13시", "☀️", 24),
            HourlyForecast("14시", "🌤️", 25),
            HourlyForecast("15시", "🌤️", 25),
            HourlyForecast("16시", "☁️", 24),
            HourlyForecast("17시", "☁️", 22)
        ),
        forecast = listOf(
            ForecastDay("오늘", "맑음", 25, 16),
            ForecastDay("금", "구름많음", 22, 15),
            ForecastDay("토", "비", 18, 13),
            ForecastDay("일", "맑음", 20, 14),
            ForecastDay("월", "맑음", 26, 17)
        )
    ),
    WeatherData(
        city = "부산",
        currentTemp = 26,
        feelsLike = 28,
        condition = "구름많음",
        humidity = 72,
        windSpeed = 5.1,
        precipitationProbability = 35,
        uvIndex = 5,
        airQuality = "좋음",
        hourlyForecast = listOf(
            HourlyForecast("지금", "☁️", 26),
            HourlyForecast("13시", "☁️", 27),
            HourlyForecast("14시", "🌧️", 26),
            HourlyForecast("15시", "🌧️", 25),
            HourlyForecast("16시", "☁️", 25),
            HourlyForecast("17시", "🌤️", 24)
        ),
        forecast = listOf(
            ForecastDay("오늘", "구름많음", 27, 20),
            ForecastDay("금", "비", 24, 19),
            ForecastDay("토", "비", 22, 18),
            ForecastDay("일", "맑음", 25, 19),
            ForecastDay("월", "맑음", 28, 21)
        )
    ),
    WeatherData(
        city = "제주",
        currentTemp = 28,
        feelsLike = 30,
        condition = "맑음",
        humidity = 68,
        windSpeed = 6.8,
        precipitationProbability = 20,
        uvIndex = 7,
        airQuality = "좋음",
        hourlyForecast = listOf(
            HourlyForecast("지금", "☀️", 28),
            HourlyForecast("13시", "☀️", 29),
            HourlyForecast("14시", "☀️", 30),
            HourlyForecast("15시", "🌤️", 30),
            HourlyForecast("16시", "🌤️", 29),
            HourlyForecast("17시", "☁️", 27)
        ),
        forecast = listOf(
            ForecastDay("오늘", "맑음", 29, 22),
            ForecastDay("금", "맑음", 30, 23),
            ForecastDay("토", "구름많음", 27, 21),
            ForecastDay("일", "비", 24, 20),
            ForecastDay("월", "맑음", 25, 21)
        )
    )
)
