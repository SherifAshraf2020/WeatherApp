package com.example.weatherapp.presentation.FavoriteDetailsScreen

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.BuildConfig
import com.example.weatherapp.R
import com.example.weatherapp.data.repository.WeatherRepository
import com.example.weatherapp.presentation.home.WeatherUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FavoriteDetailsViewModel(
    private val repository: WeatherRepository,
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    fun fetchWeather(lat: Double, lon: Double) {
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading

            val unit = repository.getUserUnitSymbol()
            val timeFormat = repository.getSavedTimeFormat()
            val windUnit = repository.getSavedWindUnit()
            val pressureUnit = repository.getSavedPressureUnit()
            val precipUnit = repository.getSavedPrecipitationUnit()

            repository.getHomeWeather(lat, lon, BuildConfig.API_KEY)
                .onSuccess { data ->
                    _uiState.value = WeatherUiState.Success(
                        data = data,
                        unit = unit,
                        timeFormat = timeFormat,
                        windUnit = windUnit,
                        pressureUnit = pressureUnit,
                        precipUnit = precipUnit,
                        address = ""
                    )
                }
                .onFailure {
                    _uiState.value = WeatherUiState.Error(context.getString(R.string.failed_load_weather))
                }
        }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val repository: WeatherRepository,
        private val context: Context
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(FavoriteDetailsViewModel::class.java)) {
                return FavoriteDetailsViewModel(repository, context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
