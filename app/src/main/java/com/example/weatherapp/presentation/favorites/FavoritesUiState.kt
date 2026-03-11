package com.example.weatherapp.presentation.favorites

import com.example.weatherapp.data.datasource.local.entities.FavoriteEntity

sealed class FavoritesUiState {
    object Loading : FavoritesUiState()
    data class Success(val favorites: List<FavoriteEntity>) : FavoritesUiState()
    data class Error(val message: String) : FavoritesUiState()
}