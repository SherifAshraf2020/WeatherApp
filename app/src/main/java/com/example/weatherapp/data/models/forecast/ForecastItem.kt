package com.example.weatherapp.data.models.forecast


import com.example.weatherapp.data.models.common.Clouds
import com.example.weatherapp.data.models.common.Main
import com.example.weatherapp.data.models.common.Sys
import com.example.weatherapp.data.models.common.Weather
import com.example.weatherapp.data.models.common.Wind
import com.google.gson.annotations.SerializedName

data class ForecastItem(
    @SerializedName("clouds")
    val clouds: Clouds,
    @SerializedName("dt")
    val dt: Int,
    @SerializedName("dt_txt")
    val dtTxt: String,
    @SerializedName("main")
    val main: Main,
    @SerializedName("pop")
    val pop: Double,
    @SerializedName("sys")
    val sys: Sys,
    @SerializedName("visibility")
    val visibility: Int,
    @SerializedName("weather")
    val weather: List<Weather>,
    @SerializedName("wind")
    val wind: Wind
)