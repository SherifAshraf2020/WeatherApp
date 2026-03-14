package com.example.weatherapp.data.datasource.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites_table")
data class FavoriteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val cityNameAr: String,
    val cityNameEn: String,
    val latitude: Double,
    val longitude: Double,
    val countryAr: String? = null,
    val countryEn: String? = null
)