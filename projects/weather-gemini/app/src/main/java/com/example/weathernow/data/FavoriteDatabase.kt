package com.example.weathernow.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Database
import androidx.room.RoomDatabase

@Entity(tableName = "favorite_city")
data class FavoriteCity(
    @PrimaryKey val id: Int,
    val cityName: String?
)

@Dao
interface FavoriteCityDao {
    @Query("SELECT * FROM favorite_city WHERE id = 1")
    suspend fun getFavoriteCity(): FavoriteCity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setFavoriteCity(favoriteCity: FavoriteCity)

    @Query("DELETE FROM favorite_city WHERE id = 1")
    suspend fun deleteFavoriteCity(): Int
}

@Database(entities = [FavoriteCity::class], version = 1)
abstract class WeatherDatabase : RoomDatabase() {
    abstract fun favoriteCityDao(): FavoriteCityDao

    companion object {
        @Volatile
        private var INSTANCE: WeatherDatabase? = null

        fun getDatabase(context: android.content.Context): WeatherDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    WeatherDatabase::class.java,
                    "weather_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
