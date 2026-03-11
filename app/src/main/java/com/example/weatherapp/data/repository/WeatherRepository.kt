package com.example.weatherapp.data.repository

import com.example.weatherapp.data.Constants
import com.example.weatherapp.data.datasource.local.PreferenceManager
import com.example.weatherapp.data.datasource.local.WeatherLocalDataSource
import com.example.weatherapp.data.datasource.local.entities.FavoriteEntity
import com.example.weatherapp.data.datasource.remote.WeatherRemoteDataSource
import com.example.weatherapp.data.models.home.FullWeatherData
import kotlinx.coroutines.flow.Flow

class WeatherRepository(
    val remoteDataSource: WeatherRemoteDataSource,
    private val localDataSource: WeatherLocalDataSource,
    private val preferenceManager: PreferenceManager
) {


    fun getSavedFavorites(): Flow<List<FavoriteEntity>> {
        return localDataSource.getFavorites()
    }

    suspend fun saveLocationToFavorites(favorite: FavoriteEntity) {
        localDataSource.saveFavorite(favorite)
    }

    suspend fun removeFromFavorites(favorite: FavoriteEntity) {
        localDataSource.removeFavorite(favorite)
    }


    suspend fun getHomeWeather(lat: Double, lon: Double, apiKey: String): Result<FullWeatherData> {
        val selectedUnit = preferenceManager.getTempUnit()

        val apiUnit = when (selectedUnit) {
            "C" -> "metric"
            "F" -> "imperial"
            else -> "metric"
        }

        val currentResult = remoteDataSource.getCurrentWeather(lat, lon, apiUnit, apiKey)
        val forecastResult = remoteDataSource.getForecast(lat, lon, apiKey, apiUnit)

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


    fun getUserUnitSymbol(): String {
        val unit = preferenceManager.getTempUnit()
        return if (unit == "imperial" || unit == "F") "F" else "C"
    }

    fun isFirstTimeUser(): Boolean {
        return preferenceManager.isFirstRun()
    }

    fun saveInitialSetup(tempUnit: String, timeFormat: String, windUnit: String) {
        preferenceManager.saveSettings(tempUnit, timeFormat, windUnit)
        preferenceManager.setFirstRun(false)
    }
}