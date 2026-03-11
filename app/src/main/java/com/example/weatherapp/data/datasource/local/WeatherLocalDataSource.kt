package com.example.weatherapp.data.datasource.local

import com.example.weatherapp.data.datasource.local.entities.FavoriteEntity
import kotlinx.coroutines.flow.Flow

class WeatherLocalDataSource(private val favoriteDao: FavoriteDao) {
    fun getFavorites() = favoriteDao.getAllFavorites()
    suspend fun saveFavorite(favorite: FavoriteEntity) = favoriteDao.insertFavorite(favorite)
    suspend fun removeFavorite(favorite: FavoriteEntity) = favoriteDao.deleteFavorite(favorite)
}