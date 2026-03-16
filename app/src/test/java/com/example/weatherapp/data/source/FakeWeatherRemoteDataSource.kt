package com.example.weatherapp.data.source

import com.example.weatherapp.data.datasource.remote.IWeatherRemoteDataSource
import com.example.weatherapp.data.models.current.CurrentWeatherResponse
import com.example.weatherapp.data.models.forecast.ForecastResponse

class FakeRemoteDataSource : IWeatherRemoteDataSource {
    var hasError = false
    var currentData: CurrentWeatherResponse? = null
    var forecastData: ForecastResponse? = null

    override suspend fun getCurrentWeather(lat: Double, lon: Double, units: String, apiKey: String, lang: String) =
        if (hasError) Result.failure(Exception("Error"))
        else currentData?.let { Result.success(it) } ?: Result.failure(Exception("Empty"))

    override suspend fun getForecast(lat: Double, lon: Double, apiKey: String, units: String, lang: String) =
        if (hasError) Result.failure(Exception("Error"))
        else forecastData?.let { Result.success(it) } ?: Result.failure(Exception("Empty"))
}