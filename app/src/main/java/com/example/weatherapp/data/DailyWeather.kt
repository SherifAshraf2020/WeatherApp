package com.example.weatherapp.data

import com.google.gson.annotations.SerializedName

data class DailyWeather(

    @SerializedName("dt") val dt: Long,

    @SerializedName("temp") val temp: TempRange,

    @SerializedName("weather") val weather: List<WeatherDescription>

)