package com.example.weatherapp.presentation

import com.example.weatherapp.data.models.home.FullWeatherData

sealed class WeatherUiState {
    object Loading : WeatherUiState()
    object PermissionRequired : WeatherUiState()
    data class Success(val data: FullWeatherData) : WeatherUiState()
    data class Error(val message: String) : WeatherUiState()
}