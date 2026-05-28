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
    val lastUpdated: String,
    val isFavorite: Boolean = false,
    val hourlyForecast: List<HourlyForecast>,
    val forecast: List<ForecastDay>
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
    val temp: Int
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
        airQuality = "좋음",
        lastUpdated = "14:00",
        hourlyForecast = listOf(
            HourlyForecast("14:00", "맑음", 23),
            HourlyForecast("15:00", "맑음", 24),
            HourlyForecast("16:00", "맑음", 24),
            HourlyForecast("17:00", "구름많음", 23),
            HourlyForecast("18:00", "구름많음", 21),
            HourlyForecast("19:00", "구름많음", 19)
        ),
        forecast = listOf(
            ForecastDay("월", "맑음", 25, 16),
            ForecastDay("화", "구름많음", 22, 15),
            ForecastDay("수", "비", 18, 13),
            ForecastDay("목", "맑음", 20, 14),
            ForecastDay("금", "맑음", 26, 17)
        )
    ),
    WeatherData(
        city = "부산",
        currentTemp = 26,
        feelsLike = 28,
        condition = "구름많음",
        humidity = 72,
        windSpeed = 5.1,
        precipitationProbability = 30,
        uvIndex = 4,
        airQuality = "보통",
        lastUpdated = "14:00",
        hourlyForecast = listOf(
            HourlyForecast("14:00", "구름많음", 26),
            HourlyForecast("15:00", "구름많음", 27),
            HourlyForecast("16:00", "비", 25),
            HourlyForecast("17:00", "비", 24),
            HourlyForecast("18:00", "구름많음", 23),
            HourlyForecast("19:00", "맑음", 22)
        ),
        forecast = listOf(
            ForecastDay("월", "구름많음", 27, 20),
            ForecastDay("화", "비", 24, 19),
            ForecastDay("수", "비", 22, 18),
            ForecastDay("목", "맑음", 25, 19),
            ForecastDay("금", "맑음", 28, 21)
        )
    ),
    WeatherData(
        city = "제주",
        currentTemp = 28,
        feelsLike = 30,
        condition = "맑음",
        humidity = 68,
        windSpeed = 6.8,
        precipitationProbability = 5,
        uvIndex = 8,
        airQuality = "좋음",
        lastUpdated = "14:00",
        hourlyForecast = listOf(
            HourlyForecast("14:00", "맑음", 28),
            HourlyForecast("15:00", "맑음", 29),
            HourlyForecast("16:00", "맑음", 29),
            HourlyForecast("17:00", "맑음", 28),
            HourlyForecast("18:00", "맑음", 27),
            HourlyForecast("19:00", "맑음", 26)
        ),
        forecast = listOf(
            ForecastDay("월", "맑음", 29, 22),
            ForecastDay("화", "맑음", 30, 23),
            ForecastDay("수", "구름많음", 27, 21),
            ForecastDay("목", "비", 24, 20),
            ForecastDay("금", "맑음", 25, 21)
        )
    )
)
