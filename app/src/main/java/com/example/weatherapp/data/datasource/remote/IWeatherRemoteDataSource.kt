package com.example.weatherapp.data.datasource.remote

import com.example.weatherapp.data.models.current.CurrentWeatherResponse
import com.example.weatherapp.data.models.forecast.ForecastResponse

interface IWeatherRemoteDataSource {
    suspend fun getCurrentWeather(
        lat: Double,
        lon: Double,
        units: String,
        apiKey: String,
        lang: String
    ): Result<CurrentWeatherResponse>

    suspend fun getForecast(
        lat: Double,
        lon: Double,
        apiKey: String,
        units: String,
        lang: String
    ): Result<ForecastResponse>
}