package com.example.weatherapp.presentation

import android.content.Context
import android.location.Geocoder
import android.location.Location
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.BuildConfig
import com.example.weatherapp.data.Constants
import com.example.weatherapp.data.FusedLocationHelper
import com.example.weatherapp.data.WeatherRepository
import com.example.weatherapp.data.models.home.FullWeatherData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class WeatherViewModel
    (
    private val repository: WeatherRepository,
    private val locationHelper: FusedLocationHelper,
    private val context: Context
) : ViewModel() {

    private val _locationState = MutableStateFlow<Location?>(null)
    val locationState: StateFlow<Location?> = _locationState.asStateFlow()

    private val _addressState = MutableStateFlow("Waiting...")
    val addressState: StateFlow<String> = _addressState.asStateFlow()

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState

    private val _eventFlow = MutableSharedFlow<WeatherEvent>()
    val eventFlow = _eventFlow.asSharedFlow()


    init {
        checkAppStartStatus()
    }

    private fun checkAppStartStatus() {
        if (repository.isFirstTimeUser()) {
            _uiState.value = WeatherUiState.SetupRequired
        } else {

            _eventFlow.tryEmit(WeatherEvent.RequestLocationPermission)
        }
    }

    fun startGettingLocation() {
        locationHelper.getFreshLocation { location ->
            _locationState.value = location
            updateAddress(location)
            fetchWeather(location.latitude, location.longitude)
        }
    }

    private fun updateAddress(location: Location) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    val addressText =
                        "${address.thoroughfare ?: "Unknown St"}, ${address.locality ?: "City"}"
                    withContext(Dispatchers.Main) {
                        _addressState.value = addressText
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _addressState.value = "Address not found"
                }
            }
        }
    }


    fun onSetupDoneClicked(tempUnit: String, timeFormat: String, windUnit: String) {
        viewModelScope.launch {
            repository.saveInitialSetup(tempUnit, timeFormat, windUnit)
            _uiState.value = WeatherUiState.Loading
            _eventFlow.emit(WeatherEvent.SetupCompleted)
            checkStatusAndFetch(
                isPermissionGranted = false,
                isNetworkAvailable = true,
                isGpsEnabled = true
            )
        }

    }

    fun onMenuClicked() {
        viewModelScope.launch {
            _eventFlow.emit(WeatherEvent.OpenNavigationDrawer)
        }
    }

    fun onPageIndicatorClicked(pageIndex: Int) {
        viewModelScope.launch {
            _eventFlow.emit(WeatherEvent.ScrollToPage(pageIndex))
        }
    }

    fun checkStatusAndFetch(
        isPermissionGranted: Boolean,
        isNetworkAvailable: Boolean,
        isGpsEnabled: Boolean
    ) {
        viewModelScope.launch {
            if (!isPermissionGranted) {
                _uiState.value = WeatherUiState.PermissionRequired
                _eventFlow.emit(WeatherEvent.RequestLocationPermission)
                return@launch
            }

            if (!isNetworkAvailable) {
                _eventFlow.emit(WeatherEvent.NetworkNotFound)
                return@launch
            }

            if (!isGpsEnabled) {
                _eventFlow.emit(WeatherEvent.GpsNotEnabled)
                return@launch
            }

            startGettingLocation()
        }
    }


    private fun fetchWeather(lat: Double, lon: Double) {

        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading
            repository.getHomeWeather(lat, lon, BuildConfig.API_KEY)
                .onSuccess { data ->
                    val processed = processWeatherData(data)
                    _uiState.value = WeatherUiState.Success(processed)

                }.onFailure {
                    _uiState.value = WeatherUiState.Error("Failed to load weather")
                }
        }
    }


    private fun processWeatherData(data: FullWeatherData): FullWeatherData {
        val hourly = data.forecast.list.take(8)
        val daily = data.forecast.list.filter { it.dtTxt.contains("12:00:00") }
        return data.copy(forecast = data.forecast.copy(list = hourly + daily))
    }
}


class WeatherViewModelFactory(
    private val repository: WeatherRepository,
    private val locationHelper: FusedLocationHelper,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return WeatherViewModel(repository, locationHelper, context) as T
    }
}