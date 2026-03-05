package com.example.weatherapp.data

data class HourlyWeather(
    val dt: Long,
    val temp: Double,
    val weather: List<WeatherDescription>
)