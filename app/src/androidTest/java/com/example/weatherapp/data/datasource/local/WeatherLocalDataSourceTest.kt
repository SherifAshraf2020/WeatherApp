package com.example.weatherapp.data.datasource.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.example.weatherapp.data.datasource.local.entities.FavoriteEntity
import com.example.weatherapp.data.db.WeatherDatabase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@MediumTest
class WeatherLocalDataSourceTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: WeatherDatabase
    private lateinit var localDataSource: WeatherLocalDataSource

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            WeatherDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        localDataSource = WeatherLocalDataSource(database.favoriteDao())
    }

    @After
    fun cleanUp() {
        database.close()
    }

    @Test
    fun saveFavorite_getFavorites_returnsSavedItem() = runTest {
        // GIVEN - A new favorite location
        val favorite = FavoriteEntity(
            cityNameAr = "القاهرة",
            cityNameEn = "Cairo",
            latitude = 30.0444,
            longitude = 31.2357
        )

        // WHEN - Saving the favorite
        localDataSource.saveFavorite(favorite)

        // THEN - Retrieving all favorites returns the list containing the saved item
        val result = localDataSource.getFavorites().first()

        assertThat(result.size, `is`(1))
        assertThat(result[0].cityNameEn, `is`(favorite.cityNameEn))
        assertThat(result[0].latitude, `is`(favorite.latitude))
    }

    @Test
    fun removeFavorite_listIsCleared() = runTest {
        // GIVEN - An item is already saved
        val favorite = FavoriteEntity(
            cityNameAr = "الأقصر",
            cityNameEn = "Luxor",
            latitude = 25.6872,
            longitude = 32.6396
        )
        localDataSource.saveFavorite(favorite)

        // Retrieve the item from the database to get the auto-generated ID
        val savedFavorites = localDataSource.getFavorites().first()
        val itemToDelete = savedFavorites[0]

        // WHEN - Removing the favorite
        localDataSource.removeFavorite(itemToDelete)

        // THEN - The list of favorites should now be empty
        val result = localDataSource.getFavorites().first()
        assertThat(result.isEmpty(), `is`(true))
    }
}
