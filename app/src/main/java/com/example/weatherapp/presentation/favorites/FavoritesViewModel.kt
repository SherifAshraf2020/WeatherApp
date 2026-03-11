package com.example.weatherapp.presentation.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.data.datasource.local.entities.FavoriteEntity
import com.example.weatherapp.data.repository.WeatherRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class FavoritesViewModel(private val repository: WeatherRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<FavoritesUiState>(FavoritesUiState.Loading)
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<FavoritesUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    init {
        fetchFavorites()
    }

    private fun fetchFavorites() {
        viewModelScope.launch {
            repository.getSavedFavorites()
                .catch { e ->
                    _uiState.value = FavoritesUiState.Error(e.message ?: "Unknown Error")
                }
                .collect { list ->
                    _uiState.value = FavoritesUiState.Success(list)
                }
        }
    }


    fun onLocationSelected(favorite: FavoriteEntity) {
        viewModelScope.launch {
            _uiEvent.emit(
                FavoritesUiEvent.NavigateToWeatherDetails(
                    lat = favorite.latitude,
                    lon = favorite.longitude,
                    cityName = favorite.cityName
                )
            )
        }
    }


    fun onAddLocationClicked() {
        viewModelScope.launch {
            _uiEvent.emit(FavoritesUiEvent.NavigateToMap)
        }
    }


    fun onDeleteLocation(favorite: FavoriteEntity) {
        viewModelScope.launch {
            repository.removeFromFavorites(favorite)
            _uiEvent.emit(FavoritesUiEvent.ShowToast(favorite.cityName))        }
    }
}