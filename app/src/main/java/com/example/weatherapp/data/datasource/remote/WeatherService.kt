package com.example.weatherapp.data.datasource.remote

import com.example.weatherapp.data.Constants
import com.example.weatherapp.data.models.current.CurrentWeatherResponse
import com.example.weatherapp.data.models.forecast.ForecastResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {
    @GET(Constants.WEATHER_ENDPOINT)
    suspend fun getCurrentWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = Constants.UNITS,
        @Query("lang") lang: String
    ): Response<CurrentWeatherResponse>

    @GET(Constants.FORECAST_ENDPOINT)
    suspend fun getForecast(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = Constants.UNITS,
        @Query("lang") lang: String
    ): Response<ForecastResponse>
}