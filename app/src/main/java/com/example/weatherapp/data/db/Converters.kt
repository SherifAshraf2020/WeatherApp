package com.example.weatherapp.data.db

import androidx.room.TypeConverter
import com.example.weatherapp.data.models.current.CurrentWeatherResponse
import com.example.weatherapp.data.models.forecast.ForecastResponse
import com.google.gson.Gson

class Converters {
    @TypeConverter
    fun fromCurrentWeatherResponse(value: CurrentWeatherResponse): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toCurrentWeatherResponse(value: String): CurrentWeatherResponse {
        return Gson().fromJson(value, CurrentWeatherResponse::class.java)
    }

    @TypeConverter
    fun fromForecastResponse(value: ForecastResponse): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toForecastResponse(value: String): ForecastResponse {
        return Gson().fromJson(value, ForecastResponse::class.java)
    }
}
