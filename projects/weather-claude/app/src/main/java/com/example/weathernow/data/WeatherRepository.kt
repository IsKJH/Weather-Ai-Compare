package com.example.weathernow.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.round

object WeatherRepository {
    private val ZONE = ZoneId.of("Asia/Seoul")
    private val HOURLY_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
    private val DAYS_KR = listOf("일", "월", "화", "수", "목", "금", "토")

    suspend fun fetchWeather(city: CityInfo): WeatherData = withContext(Dispatchers.IO) {
        coroutineScope {
            val weatherDeferred = async {
                JSONObject(URL(buildWeatherUrl(city.lat, city.lon)).readText())
            }
            val aqiDeferred = async {
                try {
                    JSONObject(URL(buildAqiUrl(city.lat, city.lon)).readText())
                } catch (e: Exception) {
                    null
                }
            }
            val weatherJson = weatherDeferred.await()
            val aqiJson = aqiDeferred.await()
            val airQuality = aqiJson?.let { parseAqiJson(it) } ?: "보통"
            parseWeatherJson(weatherJson, city.name, LocalDateTime.now(ZONE), airQuality)
        }
    }

    private fun buildWeatherUrl(lat: Double, lon: Double): String =
        "https://api.open-meteo.com/v1/forecast?" +
            "latitude=$lat&longitude=$lon" +
            "&current=temperature_2m,apparent_temperature,relative_humidity_2m," +
            "weather_code,wind_speed_10m,precipitation_probability,uv_index" +
            "&hourly=temperature_2m,weather_code" +
            "&daily=weather_code,temperature_2m_max,temperature_2m_min" +
            "&timezone=Asia%2FSeoul&forecast_days=6"

    private fun buildAqiUrl(lat: Double, lon: Double): String =
        "https://air-quality-api.open-meteo.com/v1/air-quality?" +
            "latitude=$lat&longitude=$lon&current=european_aqi"

    private fun parseAqiJson(json: JSONObject): String {
        val aqi = json.getJSONObject("current").optInt("european_aqi", 30)
        return when {
            aqi <= 20 -> "좋음"
            aqi <= 40 -> "보통"
            else -> "나쁨"
        }
    }

    private fun parseWeatherJson(
        json: JSONObject,
        cityName: String,
        now: LocalDateTime,
        airQuality: String
    ): WeatherData {
        val current = json.getJSONObject("current")
        val hourly = json.getJSONObject("hourly")
        val daily = json.getJSONObject("daily")

        val currentTemp = current.getDouble("temperature_2m").toInt()
        val feelsLike = current.getDouble("apparent_temperature").toInt()
        val humidity = current.getInt("relative_humidity_2m")
        val windSpeed = round(current.getDouble("wind_speed_10m") * 10) / 10.0
        val precipProb = current.optInt("precipitation_probability", 0)
        val uvIndex = current.optDouble("uv_index", 0.0).toInt()
        val condition = wmoToCondition(current.getInt("weather_code"))

        val hourlyForecast = parseHourlyForecast(hourly, now)
        val forecast = parseDailyForecast(daily)

        return WeatherData(
            city = cityName,
            currentTemp = currentTemp,
            feelsLike = feelsLike,
            condition = condition,
            humidity = humidity,
            windSpeed = windSpeed,
            precipitationProbability = precipProb,
            uvIndex = uvIndex,
            airQuality = airQuality,
            hourlyForecast = hourlyForecast,
            forecast = forecast
        )
    }

    private fun parseHourlyForecast(hourly: JSONObject, now: LocalDateTime): List<HourlyForecast> {
        val times = hourly.getJSONArray("time")
        val temps = hourly.getJSONArray("temperature_2m")
        val wmos = hourly.getJSONArray("weather_code")

        val nowRounded = now.withMinute(0).withSecond(0).withNano(0)
        val firstTime = LocalDateTime.parse(times.getString(0), HOURLY_FMT)
        val startIdx = maxOf(0, Duration.between(firstTime, nowRounded).toHours().toInt())

        return (startIdx until minOf(startIdx + 7, times.length())).map { i ->
            val hour = times.getString(i).substring(11, 13).toInt()
            HourlyForecast(
                time = "${hour.toString().padStart(2, '0')}시",
                condition = wmoToCondition(wmos.getInt(i)),
                temperature = temps.getDouble(i).toInt()
            )
        }
    }

    private fun parseDailyForecast(daily: JSONObject): List<ForecastDay> {
        val times = daily.getJSONArray("time")
        val wmos = daily.getJSONArray("weather_code")
        val maxTemps = daily.getJSONArray("temperature_2m_max")
        val minTemps = daily.getJSONArray("temperature_2m_min")

        return (1 until minOf(6, times.length())).map { i ->
            val date = LocalDate.parse(times.getString(i))
            ForecastDay(
                day = DAYS_KR[date.dayOfWeek.value % 7],
                condition = wmoToCondition(wmos.getInt(i)),
                high = maxTemps.getDouble(i).toInt(),
                low = minTemps.getDouble(i).toInt()
            )
        }
    }
}
