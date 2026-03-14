package com.example.weatherapp.presentation.home

sealed class WeatherEvent {

    object SetupCompleted : WeatherEvent()
    object OpenNavigationDrawer : WeatherEvent()
    data class ScrollToPage(val pageIndex: Int) : WeatherEvent()
    data class ShowToast(val message: String) : WeatherEvent()

    object GpsNotEnabled : WeatherEvent()
    object NetworkNotFound : WeatherEvent()
    object RequestLocationPermission : WeatherEvent()
    object LanguageChanged : WeatherEvent()
}