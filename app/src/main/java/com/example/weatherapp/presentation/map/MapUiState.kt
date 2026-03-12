package com.example.weatherapp.presentation.map

sealed class MapUiState {
    object Idle : MapUiState()
    object Saving : MapUiState()
    data class Error(val message: String) : MapUiState()
}