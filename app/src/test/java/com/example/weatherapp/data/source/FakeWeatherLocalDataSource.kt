package com.example.weatherapp.data.source

import com.example.weatherapp.data.datasource.local.IWeatherLocalDataSource
import com.example.weatherapp.data.datasource.local.entities.FavoriteEntity
import com.example.weatherapp.data.datasource.local.entities.HomeWeatherEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class FakeWeatherLocalDataSource(val favorites: MutableList<FavoriteEntity> = mutableListOf()) :
    IWeatherLocalDataSource {
    
    private val homeWeatherFlow = MutableStateFlow<HomeWeatherEntity?>(null)

    override fun getFavorites() = flow { emit(favorites) }
    override suspend fun saveFavorite(favorite: FavoriteEntity) { favorites.add(favorite) }
    override suspend fun removeFavorite(favorite: FavoriteEntity) { favorites.remove(favorite) }

    override fun getHomeWeather(): Flow<HomeWeatherEntity?> = homeWeatherFlow
    override suspend fun saveHomeWeather(homeWeather: HomeWeatherEntity) {
        homeWeatherFlow.value = homeWeather
    }
    override suspend fun clearHomeWeather() {
        homeWeatherFlow.value = null
    }
}
