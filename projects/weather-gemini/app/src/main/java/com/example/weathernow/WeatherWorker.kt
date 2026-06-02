package com.example.weathernow

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import android.util.Log
import com.example.weathernow.data.WeatherDatabase

class WeatherWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("WeatherWorker", "doWork started")
        return try {
            val database = WeatherDatabase.getDatabase(applicationContext)
            val favoriteCityDao = database.favoriteCityDao()
            
            val favoriteCity = favoriteCityDao.getFavoriteCity()
            Log.d("WeatherWorker", "Favorite city from DB: ${favoriteCity?.cityName}")
            if (favoriteCity?.cityName != null) {
                val cityName = favoriteCity.cityName
                val city = cities.find { it.name == cityName }
                Log.d("WeatherWorker", "Matching city found: ${city?.name}")
                if (city != null) {
                    val apiService = WeatherApiService.create()
                    val response = apiService.getForecast(city.latitude, city.longitude)
                    val condition = mapWeatherCode(response.current.weather_code)
                    Log.d("WeatherWorker", "Weather condition: ${condition}")
                    
                    showNotification(cityName, condition)
                    Log.d("WeatherWorker", "Notification shown")
                }
            }
            
            Result.success()
        } catch (e: Exception) {
            Log.e("WeatherWorker", "Error in doWork", e)
            Result.retry()
        }
    }

    private fun showNotification(cityName: String, condition: String) {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val builder = NotificationCompat.Builder(applicationContext, "weather_notifications")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("GEMINI ${cityName} 날씨 알림")
            .setContentText("현재 날씨: ${condition}")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        notificationManager.notify(1, builder.build())
    }

    // Helper functions copied or referenced from WeatherViewModel mapping logic
    private fun mapWeatherCode(code: Int): String {
        return when (code) {
            0 -> "맑음"
            1, 2, 3 -> "대체로 맑음"
            45, 48 -> "안개"
            51, 53, 55 -> "이슬비"
            61, 63, 65 -> "비"
            71, 73, 75 -> "눈"
            80, 81, 82 -> "소나기"
            95, 96, 99 -> "뇌우"
            else -> "알 수 없음"
        }
    }
}
