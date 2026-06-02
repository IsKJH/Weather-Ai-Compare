package com.example.weathernow

import android.app.Application
import com.example.weathernow.notifications.WeatherNotificationScheduler

class WeatherNowApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        WeatherNotificationScheduler.createNotificationChannel(this)
        WeatherNotificationScheduler.schedule(this)
    }
}
