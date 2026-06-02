package com.example.weathernow.worker

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.weathernow.data.CITIES
import com.example.weathernow.data.WeatherRepository
import com.example.weathernow.data.db.AppDatabase

class WeatherWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val dao = AppDatabase.getInstance(applicationContext).favoriteCityDao()
        val favorite = dao.get() ?: return Result.success()

        if (favorite.cityIndex !in CITIES.indices) return Result.success()
        val city = CITIES[favorite.cityIndex]

        val weather = try {
            WeatherRepository.fetchWeather(city)
        } catch (e: Exception) {
            return Result.retry()
        }

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentTitle("${city.name} 날씨")
            .setContentText("${weather.condition} ${weather.currentTemp}°C")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)

        return Result.success()
    }

    companion object {
        const val CHANNEL_ID = "weather_notifications"
        const val NOTIFICATION_ID = 1001
        const val WORK_NAME = "weather_periodic_update"
    }
}
