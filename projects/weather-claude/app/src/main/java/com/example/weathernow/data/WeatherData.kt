package com.example.weathernow.data

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
        city = "서울",
        currentTemp = 23,
        feelsLike = 21,
        condition = "맑음",
        humidity = 55,
        windSpeed = 3.2,
        forecast = listOf(
            ForecastDay("월", "맑음", 25, 15),
            ForecastDay("화", "구름", 22, 14),
            ForecastDay("수", "비", 18, 12),
            ForecastDay("목", "맑음", 24, 16),
            ForecastDay("금", "맑음", 26, 17)
        )
    ),
    WeatherData(
        city = "부산",
        currentTemp = 27,
        feelsLike = 29,
        condition = "흐림",
        humidity = 72,
        windSpeed = 5.1,
        forecast = listOf(
            ForecastDay("월", "흐림", 28, 20),
            ForecastDay("화", "비", 24, 19),
            ForecastDay("수", "비", 22, 18),
            ForecastDay("목", "구름", 25, 19),
            ForecastDay("금", "맑음", 27, 20)
        )
    ),
    WeatherData(
        city = "제주",
        currentTemp = 29,
        feelsLike = 32,
        condition = "맑음",
        humidity = 68,
        windSpeed = 7.3,
        forecast = listOf(
            ForecastDay("월", "맑음", 30, 22),
            ForecastDay("화", "맑음", 31, 23),
            ForecastDay("수", "구름", 28, 21),
            ForecastDay("목", "비", 25, 20),
            ForecastDay("금", "맑음", 29, 22)
        )
    )
)
