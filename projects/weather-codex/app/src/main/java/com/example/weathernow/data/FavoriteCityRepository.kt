package com.example.weathernow.data

interface FavoriteCityRepository {
    suspend fun loadFavoriteCityIndex(): Int?
    suspend fun saveFavoriteCityIndex(index: Int?)
}

object NoOpFavoriteCityRepository : FavoriteCityRepository {
    override suspend fun loadFavoriteCityIndex(): Int? = null

    override suspend fun saveFavoriteCityIndex(index: Int?) = Unit
}

class RoomFavoriteCityRepository(
    private val dao: FavoriteCityDao
) : FavoriteCityRepository {
    override suspend fun loadFavoriteCityIndex(): Int? = dao.getFavoriteCityIndex()

    override suspend fun saveFavoriteCityIndex(index: Int?) {
        dao.saveFavoriteCity(FavoriteCityEntity(cityIndex = index))
    }
}
