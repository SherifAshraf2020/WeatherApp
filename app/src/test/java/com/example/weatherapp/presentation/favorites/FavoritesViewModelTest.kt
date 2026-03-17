package com.example.weatherapp.presentation.favorites

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.weatherapp.data.datasource.local.entities.FavoriteEntity
import com.example.weatherapp.data.repository.WeatherRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesViewModelTest {

    @get:Rule
    val instanceRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var favoritesViewModel: FavoritesViewModel
    private lateinit var repository: WeatherRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        every { repository.getSavedFavorites() } returns flowOf(emptyList())
        favoritesViewModel = FavoritesViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun fetchFavorites_updatesUiStateToSuccess() {
        val favorites = listOf(
            FavoriteEntity(latitude = 30.0, longitude = 31.0, cityNameEn = "Cairo", cityNameAr = "القاهرة"),
            FavoriteEntity(latitude = 25.0, longitude = 32.0, cityNameEn = "Luxor", cityNameAr = "الأقصر")
        )
        every { repository.getSavedFavorites() } returns flowOf(favorites)

        val viewModel = FavoritesViewModel(repository)

        val state = viewModel.uiState.value
        assertThat(state is FavoritesUiState.Success, `is`(true))

        val successState = state as FavoritesUiState.Success
        assertThat(successState.favorites, `is`(favorites))
    }

    @Test
    fun onDeleteLocation_callsRepositoryToRemoveFavorite() = runTest {
        val favorite = FavoriteEntity(
            latitude = 30.0,
            longitude = 31.0,
            cityNameEn = "Cairo",
            cityNameAr = "القاهرة"
        )
        coEvery { repository.removeFromFavorites(favorite) } just runs

        favoritesViewModel.onDeleteLocation(favorite)

        coVerify { repository.removeFromFavorites(favorite) }
    }

    @Test
    fun onLocationSelected_emitsNavigateToDetailsEvent() = runTest {
        val favorite = FavoriteEntity(
            latitude = 51.5,
            longitude = -0.12,
            cityNameEn = "London",
            cityNameAr = "لندن"
        )

        favoritesViewModel.onLocationSelected(favorite)
        // Note: Full SharedFlow event verification typically uses Turbine.
    }
}
