package com.example.weathernow

data class WeatherData(
    val city: String,
    val currentTemp: Int,
    val feelsLike: Int,
    val condition: String,
    val icon: String,
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
    val icon: String,
    val temperature: Int
)

data class ForecastDay(
    val day: String,
    val condition: String,
    val icon: String,
    val high: Int,
    val low: Int
)

val mockWeatherList = listOf(
    WeatherData(
        city = "서울",
        currentTemp = 23,
        feelsLike = 21,
        condition = "맑음",
        icon = "☀️",
        humidity = 55,
        windSpeed = 3.2,
        precipitationProbability = 10,
        uvIndex = 6,
        airQuality = "보통",
        hourlyForecast = listOf(
            HourlyForecast("10시", "맑음", "☀️", 23),
            HourlyForecast("11시", "맑음", "☀️", 24),
            HourlyForecast("12시", "맑음", "☀️", 25),
            HourlyForecast("13시", "구름많음", "⛅", 25),
            HourlyForecast("14시", "구름많음", "☁️", 24),
            HourlyForecast("15시", "맑음", "☀️", 23)
        ),
        forecast = listOf(
            ForecastDay("오늘", "맑음", "☀️", 25, 16),
            ForecastDay("내일", "구름많음", "☁️", 22, 15),
            ForecastDay("토요일", "비", "🌧️", 18, 13),
            ForecastDay("일요일", "맑음", "☀️", 20, 14),
            ForecastDay("월요일", "맑음", "☀️", 26, 17)
        )
    ),
    WeatherData(
        city = "부산",
        currentTemp = 26,
        feelsLike = 28,
        condition = "구름많음",
        icon = "☁️",
        humidity = 72,
        windSpeed = 5.1,
        precipitationProbability = 35,
        uvIndex = 4,
        airQuality = "좋음",
        hourlyForecast = listOf(
            HourlyForecast("10시", "구름많음", "☁️", 26),
            HourlyForecast("11시", "구름많음", "☁️", 27),
            HourlyForecast("12시", "맑음", "☀️", 28),
            HourlyForecast("13시", "맑음", "☀️", 28),
            HourlyForecast("14시", "구름많음", "⛅", 27),
            HourlyForecast("15시", "비", "🌧️", 25)
        ),
        forecast = listOf(
            ForecastDay("오늘", "구름많음", "☁️", 27, 20),
            ForecastDay("내일", "비", "🌧️", 24, 19),
            ForecastDay("토요일", "비", "🌧️", 22, 18),
            ForecastDay("일요일", "맑음", "☀️", 25, 19),
            ForecastDay("월요일", "맑음", "☀️", 28, 21)
        )
    ),
    WeatherData(
        city = "제주",
        currentTemp = 28,
        feelsLike = 30,
        condition = "맑음",
        icon = "☀️",
        humidity = 68,
        windSpeed = 6.8,
        precipitationProbability = 20,
        uvIndex = 7,
        airQuality = "좋음",
        hourlyForecast = listOf(
            HourlyForecast("10시", "맑음", "☀️", 28),
            HourlyForecast("11시", "맑음", "☀️", 29),
            HourlyForecast("12시", "맑음", "☀️", 30),
            HourlyForecast("13시", "구름많음", "⛅", 30),
            HourlyForecast("14시", "구름많음", "☁️", 29),
            HourlyForecast("15시", "비", "🌧️", 27)
        ),
        forecast = listOf(
            ForecastDay("오늘", "맑음", "☀️", 29, 22),
            ForecastDay("내일", "맑음", "☀️", 30, 23),
            ForecastDay("토요일", "구름많음", "☁️", 27, 21),
            ForecastDay("일요일", "비", "🌧️", 24, 20),
            ForecastDay("월요일", "맑음", "☀️", 25, 21)
        )
    )
)
