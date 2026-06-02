package com.example.weathernow

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.weathernow.worker.WeatherWorker
import java.util.concurrent.TimeUnit

class WeatherApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        scheduleWeatherWorker()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            WeatherWorker.CHANNEL_ID,
            "날씨 알림",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "즐겨찾기 도시의 주기적 날씨 알림"
        }
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    private fun scheduleWeatherWorker() {
        val request = PeriodicWorkRequestBuilder<WeatherWorker>(15, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            WeatherWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
