package com.example.weatherapp.data.models.home

import com.example.weatherapp.data.models.current.CurrentWeatherResponse
import com.example.weatherapp.data.models.forecast.ForecastResponse

data class FullWeatherData(
    val current: CurrentWeatherResponse,
    val forecast: ForecastResponse
)