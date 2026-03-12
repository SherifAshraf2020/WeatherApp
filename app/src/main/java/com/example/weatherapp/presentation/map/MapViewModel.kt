package com.example.weatherapp.presentation.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.data.datasource.local.entities.FavoriteEntity
import com.example.weatherapp.data.repository.WeatherRepository
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MapViewModel(private val repository: WeatherRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<MapUiState>(MapUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<MapUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()


    fun saveLocation(latLng: LatLng, cityName: String) {
        viewModelScope.launch {
            _uiState.value = MapUiState.Saving
            try {
                repository.saveLocationToFavorites(
                    FavoriteEntity(
                        cityName = cityName,
                        latitude = latLng.latitude,
                        longitude = latLng.longitude
                    )
                )
                _uiEvent.emit(MapUiEvent.LocationSaved)
            } catch (e: Exception) {
                _uiState.value = MapUiState.Error(e.message ?: "Unknown Error")
            }
        }
    }

}

@Suppress("UNCHECKED_CAST")
class MapViewModelFactory(private val repository: WeatherRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MapViewModel(repository) as T
    }
}