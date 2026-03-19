package com.example.weatherapp.data.datasource.local

import com.example.weatherapp.data.datasource.local.entities.FavoriteEntity
import com.example.weatherapp.data.datasource.local.entities.HomeWeatherEntity
import kotlinx.coroutines.flow.Flow

interface IWeatherLocalDataSource {
    fun getFavorites(): Flow<List<FavoriteEntity>>
    suspend fun saveFavorite(favorite: FavoriteEntity)
    suspend fun removeFavorite(favorite: FavoriteEntity)


    fun getHomeWeather(): Flow<HomeWeatherEntity?>


    suspend fun saveHomeWeather(homeWeather: HomeWeatherEntity)


    suspend fun clearHomeWeather()
}