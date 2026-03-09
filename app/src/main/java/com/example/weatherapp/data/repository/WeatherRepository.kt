package com.example.weatherapp.data.repository

import com.example.weatherapp.data.Constants
import com.example.weatherapp.data.datasource.local.PreferenceManager
import com.example.weatherapp.data.datasource.remote.WeatherRemoteDataSource
import com.example.weatherapp.data.models.home.FullWeatherData

class WeatherRepository(
    val remoteDataSource: WeatherRemoteDataSource,
    private val preferenceManager: PreferenceManager
) {

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

    fun isFirstTimeUser(): Boolean {
        return preferenceManager.isFirstRun()
    }

    fun saveInitialSetup(tempUnit: String, timeFormat: String, windUnit: String){
        preferenceManager.saveSettings(tempUnit, timeFormat, windUnit)
        preferenceManager.setFirstRun(false)
    }
}