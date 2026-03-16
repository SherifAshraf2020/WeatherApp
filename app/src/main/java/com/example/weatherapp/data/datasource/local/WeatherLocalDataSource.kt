package com.example.weatherapp.data.datasource.local

import com.example.weatherapp.data.datasource.local.entities.FavoriteEntity
import kotlinx.coroutines.flow.Flow

class WeatherLocalDataSource(private val favoriteDao: FavoriteDao): IWeatherLocalDataSource {
    override fun getFavorites() = favoriteDao.getAllFavorites()
    override suspend fun saveFavorite(favorite: FavoriteEntity) = favoriteDao.insertFavorite(favorite)
    override suspend fun removeFavorite(favorite: FavoriteEntity) = favoriteDao.deleteFavorite(favorite)
}