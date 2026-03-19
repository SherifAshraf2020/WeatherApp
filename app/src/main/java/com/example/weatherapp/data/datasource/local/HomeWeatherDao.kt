package com.example.weatherapp.data.datasource.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.weatherapp.data.datasource.local.entities.HomeWeatherEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HomeWeatherDao {
    @Query("SELECT * FROM home_weather WHERE id = 0")
    fun getHomeWeather(): Flow<HomeWeatherEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHomeWeather(homeWeather: HomeWeatherEntity)

    @Query("DELETE FROM home_weather")
    suspend fun deleteHomeWeather()
}
