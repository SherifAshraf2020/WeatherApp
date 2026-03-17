package com.example.weatherapp.presentation.map

import org.junit.Assert.*
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.weatherapp.data.repository.WeatherRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MapViewModelTest {

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    val instanceRule = InstantTaskExecutorRule()

    // Set the main coroutines dispatcher for unit testing.
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var mapViewModel: MapViewModel
    private lateinit var repository: WeatherRepository

    @Before
    fun setup() {
        // Set the Main dispatcher to a TestDispatcher.
        Dispatchers.setMain(testDispatcher)

        repository = mockk()
        mapViewModel = MapViewModel(repository)
    }

    @After
    fun tearDown() {
        // Reset the Main dispatcher to the original Main dispatcher.
        Dispatchers.resetMain()
    }

    @Test
    fun saveLocation_success_callsRepositoryToSave() = runTest {
        // Given: Location data to be saved
        val lat = 30.0
        val lon = 31.0
        val nameAr = "القاهرة"
        val nameEn = "Cairo"
        val countryAr = "مصر"
        val countryEn = "Egypt"

        // Mocking the repository to successfully save the location
        coEvery { repository.saveLocationToFavorites(any()) } just runs

        // When: Saving the location through the ViewModel
        mapViewModel.saveLocation(lat, lon, nameAr, nameEn, countryAr, countryEn)

        // Then: Verify that the repository was called exactly once with the correct data
        coVerify {
            repository.saveLocationToFavorites(match {
                it.latitude == lat &&
                        it.longitude == lon &&
                        it.cityNameEn == nameEn &&
                        it.cityNameAr == nameAr
            })
        }
    }

    @Test
    fun saveLocation_error_updatesUiStateWithError() = runTest {
        // Given: The repository throws an exception when saving
        val errorMessage = "Database Connection Failed"
        coEvery { repository.saveLocationToFavorites(any()) } throws Exception(errorMessage)

        // When: Attempting to save a location
        mapViewModel.saveLocation(0.0, 0.0, "Test", "Test", null, null)

        // Then: The UI state should be updated to MapUiState.Error with the expected message
        val state = mapViewModel.uiState.value
        assertThat(state is MapUiState.Error, `is`(true))
        assertThat((state as MapUiState.Error).message, `is`(errorMessage))
    }

    @Test
    fun saveLocation_loading_updatesUiStateToSaving() = runTest {
        // Given: A repository that takes time to save
        coEvery { repository.saveLocationToFavorites(any()) } coAnswers {
            // Check state while saving
            assertThat(mapViewModel.uiState.value is MapUiState.Saving, `is`(true))
        }

        // When: Saving a location
        mapViewModel.saveLocation(0.0, 0.0, "Test", "Test", null, null)
    }
}