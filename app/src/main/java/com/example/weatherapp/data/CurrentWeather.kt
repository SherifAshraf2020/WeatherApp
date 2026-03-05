package com.example.weatherapp.data

import com.google.gson.annotations.SerializedName

data class CurrentWeather(
    val dt: Long,
    val temp: Double,
    @SerializedName("feels_like") val feelsLike: Double,
    val pressure: Int,
    val humidity: Int,
    @SerializedName("uvi") val uvIndex: Double,
    val visibility: Int,
    @SerializedName("wind_speed") val windSpeed: Double,
    val weather: List<WeatherDescription>
)