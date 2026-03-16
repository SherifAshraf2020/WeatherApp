package com.example.weatherapp.data.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.weatherapp.data.datasource.local.entities.FavoriteEntity
import com.example.weatherapp.data.models.common.*
import com.example.weatherapp.data.models.current.CurrentWeatherResponse
import com.example.weatherapp.data.models.forecast.City
import com.example.weatherapp.data.models.forecast.ForecastResponse
import com.example.weatherapp.data.source.FakePreferenceManager
import com.example.weatherapp.data.source.FakeRemoteDataSource
import com.example.weatherapp.data.source.FakeWeatherLocalDataSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WeatherRepositoryTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()
    
    private lateinit var repository: WeatherRepository
    private lateinit var fakeRemote: FakeRemoteDataSource
    private lateinit var fakeLocal: FakeWeatherLocalDataSource
    private lateinit var fakePrefs: FakePreferenceManager

    // Test Data
    private val cairoFavorite = FavoriteEntity(1, "القاهرة", "Cairo", 30.0, 31.0)
    private val alexFavorite = FavoriteEntity(2, "الإسكندرية", "Alex", 31.0, 29.0)

    @Before
    fun setup() {
        fakeRemote = FakeRemoteDataSource()
        fakeLocal = FakeWeatherLocalDataSource(mutableListOf(cairoFavorite, alexFavorite))
        fakePrefs = FakePreferenceManager()

        repository = WeatherRepository(
            remoteDataSource = fakeRemote,
            localDataSource = fakeLocal,
            preferenceManager = fakePrefs
        )
    }

    @Test
    fun getHomeWeather_success_returnsRemoteData() = runTest {
        // Given: Prepare expected remote responses
        val expectedCityName = "Cairo"
        val expectedCurrent = CurrentWeatherResponse(
            base = "stations",
            clouds = Clouds(0),
            cod = 200,
            coord = Coord(30.0, 31.0),
            dt = 1618317040,
            id = 360630,
            main = Main(feelsLike = 21.0, grndLevel = 1013, humidity = 50, pressure = 1013, seaLevel = 1013, temp = 20.0, tempMax = 22.0, tempMin = 19.0),
            name = expectedCityName,
            rain = null,
            sys = Sys(country = "EG", id = 1, sunrise = 1618282518, sunset = 1618330547, type = 1),
            timezone = 7200,
            visibility = 10000,
            weather = listOf(Weather("clear sky", "01d", 800, "Clear")),
            wind = Wind(180, 5.0)
        )
        val expectedForecast = ForecastResponse(
            city = City(coord = Coord(30.0, 31.0), country = "EG", id = 360630, name = expectedCityName, population = 1000000, sunrise = 1618282518, sunset = 1618330547, timezone = 7200),
            cnt = 0, cod = "200", list = emptyList(), message = 0
        )

        fakeRemote.currentData = expectedCurrent
        fakeRemote.forecastData = expectedForecast

        // When: Action is performed
        val result = repository.getHomeWeather(30.0, 31.0, "key")
        val actualCityName = result.getOrNull()?.current?.name

        // Then: Actual data should match expected data
        assertThat(actualCityName, IsEqual(expectedCityName))
    }

    @Test
    fun getHomeWeather_error_returnsFailure() = runTest {
        // Given: Simulate remote error
        fakeRemote.hasError = true
        val expectedStatus = true // Expected to fail

        // When: Request data
        val result = repository.getHomeWeather(30.0, 31.0, "key")
        val actualStatus = result.isFailure

        // Then: Verification
        assertThat(actualStatus, IsEqual(expectedStatus))
    }

    @Test
    fun getSavedFavorites_returnsCorrectInitialList() = runTest {
        // Given: Initial list size from setup
        val expectedSize = 2
        val expectedFirstName = "Cairo"

        // When: Fetching data
        val actualList = repository.getSavedFavorites().first()

        // Then: Comparisons
        assertThat(actualList.size, IsEqual(expectedSize))
        assertThat(actualList[0].cityNameEn, IsEqual(expectedFirstName))
    }

    @Test
    fun saveLocationToFavorites_updatesLocalStorage() = runTest {
        // Given: A new city and expected new size
        val newCity = FavoriteEntity(3, "الجيزة", "Giza", 30.0, 31.2)
        val expectedSizeAfterInsert = 3
        val expectedLastCityName = "Giza"

        // When: Perform save
        repository.saveLocationToFavorites(newCity)
        val actualList = repository.getSavedFavorites().first()

        // Then: Verify storage update
        assertThat(actualList.size, IsEqual(expectedSizeAfterInsert))
        assertThat(actualList.last().cityNameEn, IsEqual(expectedLastCityName))
    }
}