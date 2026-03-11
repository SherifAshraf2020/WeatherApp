package com.example.weatherapp.presentation.favorites

import com.example.weatherapp.data.datasource.local.entities.FavoriteEntity

sealed class FavoritesUiEvent {
    data class NavigateToWeatherDetails(val lat: Double, val lon: Double, val cityName: String) : FavoritesUiEvent()
    object NavigateToMap : FavoritesUiEvent()
    data class ShowToast(val message: String) : FavoritesUiEvent()
}