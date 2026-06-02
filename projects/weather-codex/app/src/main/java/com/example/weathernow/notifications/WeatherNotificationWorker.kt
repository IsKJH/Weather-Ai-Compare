package com.example.weathernow.notifications

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.weathernow.R
import com.example.weathernow.supportedCities
import com.example.weathernow.data.WeatherNowDatabase

class WeatherNotificationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        val database = WeatherNowDatabase.getInstance(applicationContext)
        val favoriteCityIndex = database.favoriteCityDao()
            .getFavoriteCityIndex()
            ?.takeIf { it in supportedCities.indices }
            ?: return Result.success()

        if (!canPostNotifications()) {
            return Result.success()
        }

        val city = supportedCities[favoriteCityIndex]
        val condition = mockConditionForCity(favoriteCityIndex)
        val notification = NotificationCompat.Builder(
            applicationContext,
            WeatherNotificationScheduler.CHANNEL_ID
        )
            .setSmallIcon(R.drawable.ic_weather_notification)
            .setContentTitle("${city.name} 현재 날씨")
            .setContentText(condition)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("${city.name}의 현재 날씨는 $condition 입니다.")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(
            FAVORITE_WEATHER_NOTIFICATION_ID,
            notification
        )

        return Result.success()
    }

    private fun canPostNotifications(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
    }

    private fun mockConditionForCity(cityIndex: Int): String {
        return when (cityIndex) {
            0 -> "맑음"
            1 -> "구름많음"
            2 -> "소나기"
            else -> "알 수 없음"
        }
    }

    private companion object {
        const val FAVORITE_WEATHER_NOTIFICATION_ID = 1001
    }
}
