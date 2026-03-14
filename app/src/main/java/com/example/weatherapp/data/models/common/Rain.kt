package com.example.weatherapp.data.models.common

import com.google.gson.annotations.SerializedName

data class Rain(
    @SerializedName("1h")
    val h1: Double? = null,
    @SerializedName("3h")
    val h3: Double? = null
)
