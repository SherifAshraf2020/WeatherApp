package com.example.weatherapp.data.datasource.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.weatherapp.data.models.current.CurrentWeatherResponse
import com.example.weatherapp.data.models.forecast.ForecastResponse

@Entity(tableName = "home_weather")
data class HomeWeatherEntity(
    @PrimaryKey val id: Int = 0,
    val current: CurrentWeatherResponse,
    val forecast: ForecastResponse
)
