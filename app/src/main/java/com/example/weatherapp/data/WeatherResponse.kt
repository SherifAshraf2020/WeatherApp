package com.example.weatherapp.data

data class WeatherResponse(
    val lat: Double,
    val lon: Double,
    val timezone: String,
    val current: CurrentWeather,
    val hourly: List<HourlyWeather>,
    val daily: List<DailyWeather>
)