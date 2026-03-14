package com.example.weatherapp.data.models.current

import com.example.weatherapp.data.models.common.Clouds
import com.example.weatherapp.data.models.common.Coord
import com.example.weatherapp.data.models.common.Main
import com.example.weatherapp.data.models.common.Rain
import com.example.weatherapp.data.models.common.Sys
import com.example.weatherapp.data.models.common.Weather
import com.example.weatherapp.data.models.common.Wind
import com.google.gson.annotations.SerializedName

data class CurrentWeatherResponse(
    @SerializedName("base")
    val base: String,
    @SerializedName("clouds")
    val clouds: Clouds,
    @SerializedName("cod")
    val cod: Int,
    @SerializedName("coord")
    val coord: Coord,
    @SerializedName("dt")
    val dt: Int,
    @SerializedName("id")
    val id: Int,
    @SerializedName("main")
    val main: Main,
    @SerializedName("name")
    val name: String,
    @SerializedName("rain")
    val rain: Rain? = null,
    @SerializedName("sys")
    val sys: Sys,
    @SerializedName("timezone")
    val timezone: Int,
    @SerializedName("visibility")
    val visibility: Int,
    @SerializedName("weather")
    val weather: List<Weather>,
    @SerializedName("wind")
    val wind: Wind
)
