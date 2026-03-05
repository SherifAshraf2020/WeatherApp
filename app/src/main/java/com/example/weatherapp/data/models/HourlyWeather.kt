package com.example.weatherapp.data.models

data class HourlyWeather(
    val dt: Long,
    val temp: Double,
    val weather: List<WeatherDescription>
)