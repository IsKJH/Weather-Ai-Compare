package com.example.weathernow

data class City(
    val name: String,
    val latitude: Double,
    val longitude: Double
)

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

val supportedCities = listOf(
    City(name = "서울", latitude = 37.5665, longitude = 126.9780),
    City(name = "부산", latitude = 35.1796, longitude = 129.0756),
    City(name = "제주", latitude = 33.4996, longitude = 126.5312)
)
