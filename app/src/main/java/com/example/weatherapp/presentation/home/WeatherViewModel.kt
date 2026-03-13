package com.example.weatherapp.presentation.home

import android.content.Context
import android.location.Geocoder
import android.location.Location
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.BuildConfig
import com.example.weatherapp.R
import com.example.weatherapp.data.datasource.location.FusedLocationHelper
import com.example.weatherapp.data.repository.WeatherRepository
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

    private val _addressState = MutableStateFlow(context.getString(R.string.waiting))
    val addressState: StateFlow<String> = _addressState.asStateFlow()

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState

    private val _eventFlow = MutableSharedFlow<WeatherEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    var isSplashLoading = mutableStateOf(true)
        private set

    init {
        checkAppStartStatus()
    }

    private fun checkAppStartStatus() {
        if (repository.isFirstTimeUser()) {
            _uiState.value = WeatherUiState.SetupRequired
            isSplashLoading.value = false
        } else {
            viewModelScope.launch {
                _eventFlow.emit(WeatherEvent.RequestLocationPermission)
                isSplashLoading.value = false
            }
        }
    }

    fun startGettingLocation() {
        locationHelper.getFreshLocation { location ->
            _locationState.value = location
            viewModelScope.launch {
                val address = updateAddress(location)
                fetchWeather(location.latitude, location.longitude, address)
            }
        }
    }

    private suspend fun updateAddress(location: Location): String {
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    val addressText =
                        "${address.thoroughfare ?: context.getString(R.string.unknown_street)}, ${address.locality ?: context.getString(R.string.unknown_city)}"
                    withContext(Dispatchers.Main) {
                        _addressState.value = addressText
                    }
                    addressText
                } else {
                    val fallback = context.getString(R.string.address_not_found)
                    withContext(Dispatchers.Main) {
                        _addressState.value = fallback
                    }
                    fallback
                }
            } catch (e: Exception) {
                val fallback = context.getString(R.string.address_not_found)
                withContext(Dispatchers.Main) {
                    _addressState.value = fallback
                }
                fallback
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
                isSplashLoading.value = false
                _eventFlow.emit(WeatherEvent.RequestLocationPermission)
                return@launch
            }

            if (!isNetworkAvailable) {
                isSplashLoading.value = false
                _uiState.value = WeatherUiState.Error(context.getString(R.string.no_internet_error))
                _eventFlow.emit(WeatherEvent.NetworkNotFound)
                return@launch
            }

            if (!isGpsEnabled) {
                isSplashLoading.value = false
                _uiState.value = WeatherUiState.Error(context.getString(R.string.enable_gps_error))
                _eventFlow.emit(WeatherEvent.GpsNotEnabled)
                return@launch
            }

            _uiState.value = WeatherUiState.Loading
            startGettingLocation()
        }
    }

    private fun fetchWeather(lat: Double, lon: Double, address: String = "") {
        viewModelScope.launch {
            val unitSymbol = repository.getUserUnitSymbol()
            val timeFormat = repository.getSavedTimeFormat()
            val windUnit = repository.getSavedWindUnit()
            repository.getHomeWeather(lat, lon, BuildConfig.API_KEY)
                .onSuccess { data ->
                    _uiState.value = WeatherUiState.Success(
                        processWeatherData(data),
                        unitSymbol,
                        timeFormat,
                        windUnit,
                        address
                    )
                    isSplashLoading.value = false
                }.onFailure {
                    _uiState.value = WeatherUiState.Error(context.getString(R.string.failed_load_weather))
                    isSplashLoading.value = false
                }
        }
    }


    private fun processWeatherData(data: FullWeatherData): FullWeatherData {
        val hourly = data.forecast.list.take(8)
        val daily = data.forecast.list.filter { it.dtTxt.contains("12:00:00") }
        return data.copy(forecast = data.forecast.copy(list = hourly + daily))
    }
}


@Suppress("UNCHECKED_CAST")
class WeatherViewModelFactory(
    private val repository: WeatherRepository,
    private val locationHelper: FusedLocationHelper,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return WeatherViewModel(repository, locationHelper, context) as T
    }
}
