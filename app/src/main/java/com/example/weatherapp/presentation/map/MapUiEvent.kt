package com.example.weatherapp.presentation.map

sealed class MapUiEvent {
    object LocationSaved : MapUiEvent()
}