package com.example.weatherapp.data.datasource.local

import com.example.weatherapp.data.datasource.local.entities.FavoriteEntity
import kotlinx.coroutines.flow.Flow

interface IWeatherLocalDataSource {
    fun getFavorites(): Flow<List<FavoriteEntity>>
    suspend fun saveFavorite(favorite: FavoriteEntity)
    suspend fun removeFavorite(favorite: FavoriteEntity)
}