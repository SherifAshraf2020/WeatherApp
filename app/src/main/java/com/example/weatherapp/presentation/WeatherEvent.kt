package com.example.weatherapp.presentation

sealed class WeatherEvent {

    object OpenNavigationDrawer : WeatherEvent()
    data class ScrollToPage(val pageIndex: Int) : WeatherEvent()
    data class ShowToast(val message: String) : WeatherEvent()

    object GpsNotEnabled : WeatherEvent()
    object NetworkNotFound : WeatherEvent()
    object RequestLocationPermission : WeatherEvent()
}