package com.example.weathernow

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("v1/forecast")
    suspend fun getForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "temperature_2m,relative_humidity_2m,apparent_temperature,precipitation,weather_code,wind_speed_10m",
        @Query("hourly") hourly: String = "temperature_2m,weather_code",
        @Query("daily") daily: String = "weather_code,temperature_2m_max,temperature_2m_min,uv_index_max",
        @Query("timezone") timezone: String = "Asia/Seoul",
        @Query("forecast_days") forecastDays: Int = 6
    ): WeatherResponse

    companion object {
        private const val BASE_URL = "https://api.open-meteo.com/"

        fun create(): WeatherApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(WeatherApiService::class.java)
        }
    }
}

data class WeatherResponse(
    val current: CurrentWeather,
    val hourly: HourlyData,
    val daily: DailyData
)

data class CurrentWeather(
    val temperature_2m: Double,
    val relative_humidity_2m: Int,
    val apparent_temperature: Double,
    val precipitation: Double,
    val weather_code: Int,
    val wind_speed_10m: Double
)

data class HourlyData(
    val time: List<String>,
    val temperature_2m: List<Double>,
    val weather_code: List<Int>
)

data class DailyData(
    val time: List<String>,
    val weather_code: List<Int>,
    val temperature_2m_max: List<Double>,
    val temperature_2m_min: List<Double>,
    val uv_index_max: List<Double>
)
