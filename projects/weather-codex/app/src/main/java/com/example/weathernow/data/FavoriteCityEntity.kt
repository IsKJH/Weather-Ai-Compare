package com.example.weathernow.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_city_preference")
data class FavoriteCityEntity(
    @PrimaryKey val id: Int = FAVORITE_CITY_ROW_ID,
    val cityIndex: Int?
)

const val FAVORITE_CITY_ROW_ID = 0
