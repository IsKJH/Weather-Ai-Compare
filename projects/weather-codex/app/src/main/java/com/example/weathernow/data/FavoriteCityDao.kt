package com.example.weathernow.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FavoriteCityDao {
    @Query("SELECT cityIndex FROM favorite_city_preference WHERE id = :id")
    suspend fun getFavoriteCityIndex(id: Int = FAVORITE_CITY_ROW_ID): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveFavoriteCity(entity: FavoriteCityEntity)
}
