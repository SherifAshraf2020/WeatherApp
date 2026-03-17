package com.example.weatherapp.data.datasource.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
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
@SmallTest
class FavoriteDaoTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: WeatherDatabase
    private lateinit var favoriteDao: FavoriteDao

    @Before
    fun initDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            WeatherDatabase::class.java
        ).allowMainThreadQueries().build()

        favoriteDao = database.favoriteDao()
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun insertFavorite_getById() = runTest {
        // GIVEN - Insert a favorite location
        val favorite = FavoriteEntity(
            latitude = 30.0,
            longitude = 31.0,
            cityNameEn = "Cairo",
            cityNameAr = "القاهرة"
        )
        favoriteDao.insertFavorite(favorite)

        // WHEN - Get all favorites from the database
        val favoritesList = favoriteDao.getAllFavorites().first()

        // THEN - The loaded data contains the expected values
        assertThat(favoritesList.size, `is`(1))
        assertThat(favoritesList[0].cityNameEn, `is`(favorite.cityNameEn))
        assertThat(favoritesList[0].latitude, `is`(favorite.latitude))
    }

    @Test
    fun deleteFavorite_checkListEmpty() = runTest {
        // GIVEN - Insert a favorite location
        val favorite = FavoriteEntity(
            latitude = 30.0,
            longitude = 31.0,
            cityNameEn = "Cairo",
            cityNameAr = "القاهرة"
        )
        favoriteDao.insertFavorite(favorite)

        // Room auto-generates the ID, so we need to retrieve the entity with the correct ID to delete it
        val insertedFavorite = favoriteDao.getAllFavorites().first()[0]

        // WHEN - Delete the favorite location
        favoriteDao.deleteFavorite(insertedFavorite)

        // THEN - The list should be empty
        val favoritesList = favoriteDao.getAllFavorites().first()
        assertThat(favoritesList.isEmpty(), `is`(true))
    }

    @Test
    fun insertFavorite_replaceOnConflict() = runTest {
        // GIVEN - Two entities with the same ID to trigger OnConflictStrategy.REPLACE
        val initialFavorite = FavoriteEntity(
            id = 10,
            latitude = 31.2001,
            longitude = 29.9187,
            cityNameEn = "Alex",
            cityNameAr = "الإسكندرية"
        )

        val updatedFavorite = FavoriteEntity(
            id = 10, // Same ID
            latitude = 31.2001,
            longitude = 29.9187,
            cityNameEn = "Alexandria",
            cityNameAr = "الإسكندرية"
        )
        favoriteDao.insertFavorite(initialFavorite)

        // WHEN - Inserting the updated version with the same ID
        favoriteDao.insertFavorite(updatedFavorite)

        // THEN - Check if replaced correctly (size stays 1, name is updated)
        val favoritesList = favoriteDao.getAllFavorites().first()
        assertThat(favoritesList.size, `is`(1))
        assertThat(favoritesList[0].cityNameEn, `is`("Alexandria"))
    }
}
