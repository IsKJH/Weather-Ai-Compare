package com.example.weathernow.data

data class HourlyForecast(
    val time: String,
    val condition: String,
    val temp: Int
)

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
    val forecast: List<ForecastDay>,
    val hourlyForecast: List<HourlyForecast>
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
        uvIndex = 5,
        airQuality = "좋음",
        forecast = listOf(
            ForecastDay("월", "맑음", 25, 15),
            ForecastDay("화", "구름많음", 22, 14),
            ForecastDay("수", "비", 18, 12),
            ForecastDay("목", "맑음", 24, 16),
            ForecastDay("금", "맑음", 26, 17)
        ),
        hourlyForecast = listOf(
            HourlyForecast("지금", "맑음", 23),
            HourlyForecast("13시", "맑음", 24),
            HourlyForecast("14시", "맑음", 25),
            HourlyForecast("15시", "구름많음", 24),
            HourlyForecast("16시", "구름많음", 23),
            HourlyForecast("17시", "맑음", 22),
            HourlyForecast("18시", "맑음", 21)
        )
    ),
    WeatherData(
        city = "부산",
        currentTemp = 27,
        feelsLike = 29,
        condition = "흐림",
        humidity = 72,
        windSpeed = 5.1,
        precipitationProbability = 45,
        uvIndex = 3,
        airQuality = "보통",
        forecast = listOf(
            ForecastDay("월", "흐림", 28, 20),
            ForecastDay("화", "비", 24, 19),
            ForecastDay("수", "비", 22, 18),
            ForecastDay("목", "구름많음", 25, 19),
            ForecastDay("금", "맑음", 27, 20)
        ),
        hourlyForecast = listOf(
            HourlyForecast("지금", "흐림", 27),
            HourlyForecast("13시", "흐림", 27),
            HourlyForecast("14시", "비", 26),
            HourlyForecast("15시", "비", 25),
            HourlyForecast("16시", "비", 24),
            HourlyForecast("17시", "구름많음", 25),
            HourlyForecast("18시", "구름많음", 26)
        )
    ),
    WeatherData(
        city = "제주",
        currentTemp = 29,
        feelsLike = 32,
        condition = "맑음",
        humidity = 68,
        windSpeed = 7.3,
        precipitationProbability = 5,
        uvIndex = 8,
        airQuality = "나쁨",
        forecast = listOf(
            ForecastDay("월", "맑음", 30, 22),
            ForecastDay("화", "맑음", 31, 23),
            ForecastDay("수", "구름많음", 28, 21),
            ForecastDay("목", "비", 25, 20),
            ForecastDay("금", "맑음", 29, 22)
        ),
        hourlyForecast = listOf(
            HourlyForecast("지금", "맑음", 29),
            HourlyForecast("13시", "맑음", 30),
            HourlyForecast("14시", "맑음", 31),
            HourlyForecast("15시", "맑음", 30),
            HourlyForecast("16시", "구름많음", 29),
            HourlyForecast("17시", "구름많음", 28),
            HourlyForecast("18시", "맑음", 27)
        )
    )
)
