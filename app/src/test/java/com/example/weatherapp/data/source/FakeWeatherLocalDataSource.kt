package com.example.weatherapp.data.source

import com.example.weatherapp.data.datasource.local.IWeatherLocalDataSource
import com.example.weatherapp.data.datasource.local.entities.FavoriteEntity
import kotlinx.coroutines.flow.flow

class FakeWeatherLocalDataSource(val favorites: MutableList<FavoriteEntity> = mutableListOf()) :
    IWeatherLocalDataSource {
    override fun getFavorites() = flow { emit(favorites) }
    override suspend fun saveFavorite(favorite: FavoriteEntity) { favorites.add(favorite) }
    override suspend fun removeFavorite(favorite: FavoriteEntity) { favorites.remove(favorite) }
}