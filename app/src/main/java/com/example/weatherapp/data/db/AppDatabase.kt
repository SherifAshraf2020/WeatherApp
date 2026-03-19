package com.example.weatherapp.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.weatherapp.data.datasource.local.FavoriteDao
import com.example.weatherapp.data.datasource.local.HomeWeatherDao
import com.example.weatherapp.data.datasource.local.entities.FavoriteEntity
import com.example.weatherapp.data.datasource.local.entities.HomeWeatherEntity

@Database(entities = [FavoriteEntity::class, HomeWeatherEntity::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class WeatherDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun homeWeatherDao(): HomeWeatherDao

    companion object {
        @Volatile
        private var INSTANCE: WeatherDatabase? = null

        fun getDatabase(context: Context): WeatherDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WeatherDatabase::class.java,
                    "weather_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
