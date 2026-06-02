package com.example.weathernow.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FavoriteCityDao {
    @Query("SELECT * FROM favorite_city WHERE id = 1")
    suspend fun get(): FavoriteCityEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: FavoriteCityEntity)

    @Query("DELETE FROM favorite_city")
    suspend fun clear()
}
