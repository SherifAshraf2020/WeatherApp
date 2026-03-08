package com.example.weatherapp.presentation

sealed class WeatherEvent {

    data class ScrollToPage(val pageIndex: Int) : WeatherEvent()

    object GpsNotEnabled : WeatherEvent()

    object NetworkNotFound : WeatherEvent()

    object RequestLocationPermission : WeatherEvent()
}