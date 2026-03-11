package com.example.weatherapp.presentation.home

import com.example.weatherapp.data.models.home.FullWeatherData

sealed class WeatherUiState {
    object Loading : WeatherUiState()
    object SetupRequired : WeatherUiState()
    object PermissionRequired : WeatherUiState()
    data class Success(val data: FullWeatherData, val unit: String) : WeatherUiState()
    data class Error(val message: String) : WeatherUiState()
}