package com.example.weatherapp.data.worker

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.weatherapp.BuildConfig
import com.example.weatherapp.data.datasource.local.WeatherLocalDataSource
import com.example.weatherapp.data.datasource.local.sharedpreference.PreferenceManager
import com.example.weatherapp.data.datasource.remote.WeatherRemoteDataSource
import com.example.weatherapp.data.db.WeatherDatabase
import com.example.weatherapp.data.repository.WeatherRepository
import com.example.weatherapp.presentation.widget.WeatherWidget

class WeatherSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val database = WeatherDatabase.getDatabase(applicationContext)
        val preferenceManager = PreferenceManager(applicationContext)

        val repository = WeatherRepository(
            remoteDataSource = WeatherRemoteDataSource(),
            localDataSource = WeatherLocalDataSource(
                database.favoriteDao(),
                database.homeWeatherDao()
            ),
            preferenceManager = preferenceManager
        )

        // Get the latest home location from the repository/preferences
        val lat = repository.getHomeLatitude()
        val lon = repository.getHomeLongitude()

        // If no location is saved yet, we can't sync.
        if (lat == 0.0 && lon == 0.0) return Result.failure()

        return try {
            // Passing empty string for address as it's a background sync
            val result = repository.refreshHomeWeather(lat, lon, BuildConfig.API_KEY, "")
            if (result.isSuccess) {
                WeatherWidget().updateAll(applicationContext)
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
