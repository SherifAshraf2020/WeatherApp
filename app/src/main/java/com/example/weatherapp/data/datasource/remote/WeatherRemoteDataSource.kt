package com.example.weatherapp.data.datasource.remote

import com.example.weatherapp.data.models.current.CurrentWeatherResponse
import com.example.weatherapp.data.models.forecast.ForecastResponse
import com.example.weatherapp.data.network.Network

class WeatherRemoteDataSource {
    private val weatherService: WeatherService = Network.weatherService

    suspend fun getCurrentWeather(lat: Double, lon: Double, units: String, apiKey: String): Result<CurrentWeatherResponse> {
        return try {
            val response = weatherService.getCurrentWeather(lat, lon, apiKey, units)
            if (response.isSuccessful) {
                val data = response.body() ?: throw Exception("Empty body")
                Result.success(data)
            } else {
                Result.failure(Exception("Error ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getForecast(lat: Double, lon: Double, apiKey: String): Result<ForecastResponse> {
        return try {
            val response = weatherService.getForecast(lat, lon, apiKey)
            if (response.isSuccessful) {
                val data = response.body() ?: throw Exception("Empty body")
                Result.success(data)
            } else {
                Result.failure(Exception("Error ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}