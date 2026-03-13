package com.example.weatherapp.presentation.FavoriteDetailsScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.BuildConfig
import com.example.weatherapp.data.repository.WeatherRepository
import com.example.weatherapp.presentation.home.WeatherUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FavoriteDetailsViewModel(private val repository: WeatherRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    fun fetchWeather(lat: Double, lon: Double) {
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading

            val unit = repository.getUserUnitSymbol()
            val timeFormat = repository.getSavedTimeFormat()
            val windUnit = repository.getSavedWindUnit()

            repository.getHomeWeather(lat, lon, BuildConfig.API_KEY)
                .onSuccess { data ->
                    _uiState.value = WeatherUiState.Success(data, unit, timeFormat, windUnit)
                }
                .onFailure {
                    _uiState.value = WeatherUiState.Error("Failed to load weather data")
                }
        }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val repository: WeatherRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(FavoriteDetailsViewModel::class.java)) {
                return FavoriteDetailsViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}