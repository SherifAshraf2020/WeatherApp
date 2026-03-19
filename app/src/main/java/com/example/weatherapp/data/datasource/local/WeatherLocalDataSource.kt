package com.example.weatherapp.data.datasource.local

import com.example.weatherapp.data.datasource.local.entities.FavoriteEntity
import com.example.weatherapp.data.datasource.local.entities.HomeWeatherEntity
import kotlinx.coroutines.flow.Flow

class WeatherLocalDataSource(
    private val favoriteDao: FavoriteDao,
    private val homeWeatherDao: HomeWeatherDao
) : IWeatherLocalDataSource {
    override fun getFavorites() = favoriteDao.getAllFavorites()
    override suspend fun saveFavorite(favorite: FavoriteEntity) =
        favoriteDao.insertFavorite(favorite)

    override suspend fun removeFavorite(favorite: FavoriteEntity) =
        favoriteDao.deleteFavorite(favorite)


    override fun getHomeWeather(): Flow<HomeWeatherEntity?> {
        return homeWeatherDao.getHomeWeather()
    }

    override suspend fun saveHomeWeather(homeWeather: HomeWeatherEntity) {
        homeWeatherDao.insertHomeWeather(homeWeather)
    }

    override suspend fun clearHomeWeather() {
        homeWeatherDao.deleteHomeWeather()
    }
}