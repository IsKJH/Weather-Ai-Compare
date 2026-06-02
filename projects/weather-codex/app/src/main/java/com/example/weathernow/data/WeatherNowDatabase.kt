package com.example.weathernow.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [FavoriteCityEntity::class],
    version = 1,
    exportSchema = false
)
abstract class WeatherNowDatabase : RoomDatabase() {
    abstract fun favoriteCityDao(): FavoriteCityDao

    companion object {
        @Volatile
        private var instance: WeatherNowDatabase? = null

        fun getInstance(context: Context): WeatherNowDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    WeatherNowDatabase::class.java,
                    "weathernow.db"
                ).build().also { instance = it }
            }
        }
    }
}
