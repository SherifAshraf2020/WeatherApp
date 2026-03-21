package com.example.weatherapp.data.repository

import com.example.weatherapp.data.Constants
import com.example.weatherapp.data.datasource.local.sharedpreference.IPreferenceManager
import com.example.weatherapp.data.datasource.local.IWeatherLocalDataSource
import com.example.weatherapp.data.datasource.local.entities.FavoriteEntity
import com.example.weatherapp.data.datasource.local.entities.HomeWeatherEntity
import com.example.weatherapp.data.datasource.remote.IWeatherRemoteDataSource
import com.example.weatherapp.data.models.home.FullWeatherData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WeatherRepository(
    val remoteDataSource: IWeatherRemoteDataSource,
    private val localDataSource: IWeatherLocalDataSource,
    private val preferenceManager: IPreferenceManager
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

    /**
     * Observes the home weather from the local database.
     */
    fun getHomeWeatherFromLocal(): Flow<FullWeatherData?> {
        return localDataSource.getHomeWeather().map { entity ->
            entity?.let { FullWeatherData(it.current, it.forecast, it.address) }
        }
    }

    /**
     * Refreshes the home weather from the remote source and updates the local database.
     * This is the "sync" part of sync-first/offline-first.
     */
    suspend fun refreshHomeWeather(lat: Double, lon: Double, apiKey: String, address: String): Result<Unit> {
        val selectedUnit = preferenceManager.getTempUnit()
        val apiUnit = when (selectedUnit) {
            "C" -> "metric"
            "F" -> "imperial"
            else -> "metric"
        }
        val selectedLang = preferenceManager.getLanguage() ?: "en"

        val currentResult = remoteDataSource.getCurrentWeather(lat, lon, apiUnit, apiKey, selectedLang)
        val forecastResult = remoteDataSource.getForecast(lat, lon, apiKey, apiUnit, selectedLang)

        return if (currentResult.isSuccess && forecastResult.isSuccess) {
            val fullWeatherData = FullWeatherData(
                current = currentResult.getOrThrow(),
                forecast = forecastResult.getOrThrow(),
                address = address
            )
            // Save to local database (id=0 to always keep only one home weather record)
            localDataSource.saveHomeWeather(
                HomeWeatherEntity(
                    current = fullWeatherData.current,
                    forecast = fullWeatherData.forecast,
                    address = fullWeatherData.address
                )
            )
            Result.success(Unit)
        } else {
            val error = currentResult.exceptionOrNull() ?: forecastResult.exceptionOrNull()
            Result.failure(error ?: Exception(Constants.ERROR_FETCH_DATA))
        }
    }

    // Legacy method or for specific use cases where you want immediate data without observing
    suspend fun getHomeWeather(lat: Double, lon: Double, apiKey: String, address: String = ""): Result<FullWeatherData> {
        val selectedUnit = preferenceManager.getTempUnit()
        val apiUnit = when (selectedUnit) {
            "C" -> "metric"
            "F" -> "imperial"
            else -> "metric"
        }
        val selectedLang = preferenceManager.getLanguage() ?: "en"

        val currentResult = remoteDataSource.getCurrentWeather(lat, lon, apiUnit, apiKey, selectedLang)
        val forecastResult = remoteDataSource.getForecast(lat, lon, apiKey, apiUnit, selectedLang)

        return if (currentResult.isSuccess && forecastResult.isSuccess) {
            Result.success(
                FullWeatherData(
                    current = currentResult.getOrThrow(),
                    forecast = forecastResult.getOrThrow(),
                    address = address
                )
            )
        } else {
            val error = currentResult.exceptionOrNull() ?: forecastResult.exceptionOrNull()
            Result.failure(error ?: Exception(Constants.ERROR_FETCH_DATA))
        }
    }


    fun saveLanguage(lang: String) {
        preferenceManager.saveLanguage(lang)
    }

    fun getSavedLanguage(): String {
        return preferenceManager.getLanguage() ?: "en"
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

    fun getSavedTimeFormat(): String {
        return preferenceManager.getTimeFormat()
    }

    fun getSavedWindUnit(): String {
        return preferenceManager.getWindUnit()
    }

    fun savePressureUnit(unit: String) {
        preferenceManager.savePressureUnit(unit)
    }

    fun getSavedPressureUnit(): String {
        return preferenceManager.getPressureUnit()
    }

    fun savePrecipitationUnit(unit: String) {
        preferenceManager.savePrecipitationUnit(unit)
    }

    fun getSavedPrecipitationUnit(): String {
        return preferenceManager.getPrecipitationUnit()
    }


    fun isNotificationsEnabled(): Boolean {
        return preferenceManager.isNotificationsEnabled()
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        preferenceManager.setNotificationsEnabled(enabled)
    }

    fun isStatusBarEnabled(): Boolean {
        return preferenceManager.isStatusBarEnabled()
    }

    fun setStatusBarEnabled(enabled: Boolean) {
        preferenceManager.setStatusBarEnabled(enabled)
    }

    fun saveHomeLocation(lat: Double, lon: Double) {
        preferenceManager.saveHomeLocation(lat, lon)
    }

    fun getHomeLatitude(): Double {
        return preferenceManager.getHomeLatitude()
    }

    fun getHomeLongitude(): Double {
        return preferenceManager.getHomeLongitude()
    }
}
