package com.example.weathernow.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_city")
data class FavoriteCityEntity(
    @PrimaryKey val id: Int = 1,
    val cityIndex: Int
)
