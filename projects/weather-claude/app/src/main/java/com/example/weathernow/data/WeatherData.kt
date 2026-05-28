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
            HourlyForecast("06시", "맑음", 18),
            HourlyForecast("09시", "맑음", 20),
            HourlyForecast("12시", "맑음", 23),
            HourlyForecast("15시", "구름", 22),
            HourlyForecast("18시", "구름", 20),
            HourlyForecast("21시", "맑음", 17),
            HourlyForecast("00시", "맑음", 15)
        ),
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
        precipitationProbability = 40,
        uvIndex = 4,
        airQuality = "나쁨",
        hourlyForecast = listOf(
            HourlyForecast("06시", "흐림", 22),
            HourlyForecast("09시", "흐림", 24),
            HourlyForecast("12시", "비", 26),
            HourlyForecast("15시", "비", 27),
            HourlyForecast("18시", "흐림", 25),
            HourlyForecast("21시", "구름", 23),
            HourlyForecast("00시", "구름", 21)
        ),
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
        precipitationProbability = 5,
        uvIndex = 9,
        airQuality = "좋음",
        hourlyForecast = listOf(
            HourlyForecast("06시", "맑음", 24),
            HourlyForecast("09시", "맑음", 26),
            HourlyForecast("12시", "맑음", 29),
            HourlyForecast("15시", "맑음", 31),
            HourlyForecast("18시", "맑음", 28),
            HourlyForecast("21시", "구름", 26),
            HourlyForecast("00시", "맑음", 24)
        ),
        forecast = listOf(
            ForecastDay("월", "맑음", 30, 22),
            ForecastDay("화", "맑음", 31, 23),
            ForecastDay("수", "구름", 28, 21),
            ForecastDay("목", "비", 25, 20),
            ForecastDay("금", "맑음", 29, 22)
        )
    )
)
