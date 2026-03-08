package com.example.weatherapp.data

import com.example.weatherapp.BuildConfig
import com.example.weatherapp.R
import com.example.weatherapp.data.datasource.remote.WeatherRemoteDataSource
import com.example.weatherapp.data.models.current.CurrentWeatherResponse
import com.example.weatherapp.data.models.forecast.ForecastResponse
import com.example.weatherapp.data.models.home.FullWeatherData

class WeatherRepository(val remoteDataSource: WeatherRemoteDataSource) {

    suspend fun getHomeWeather(lat: Double, lon: Double, apiKey: String): Result<FullWeatherData> {

        val currentResult = remoteDataSource.getCurrentWeather(lat, lon, apiKey)
        val forecastResult = remoteDataSource.getForecast(lat, lon, apiKey)

        return if (currentResult.isSuccess && forecastResult.isSuccess) {
            Result.success(
                FullWeatherData(
                    current = currentResult.getOrThrow(),
                    forecast = forecastResult.getOrThrow()
                )
            )
        } else {
            val error = currentResult.exceptionOrNull() ?: forecastResult.exceptionOrNull()
            Result.failure(error ?: Exception(Constants.ERROR_FETCH_DATA))
        }


    }
}