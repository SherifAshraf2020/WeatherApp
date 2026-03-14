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
import com.example.weatherapp.data.util.UnitConverter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class WeatherViewModel(
    private val repository: WeatherRepository,
    private val locationHelper: FusedLocationHelper,
    private val context: Context
) : ViewModel() {

    private val _tempUnit = MutableStateFlow(repository.getUserUnitSymbol())
    val tempUnit = _tempUnit.asStateFlow()

    private val _timeFormat = MutableStateFlow(repository.getSavedTimeFormat())
    val timeFormat = _timeFormat.asStateFlow()

    private val _windUnit = MutableStateFlow(repository.getSavedWindUnit())
    val windUnit = _windUnit.asStateFlow()

    private val _pressureUnit = MutableStateFlow(repository.getSavedPressureUnit())
    val pressureUnit = _pressureUnit.asStateFlow()

    private val _precipUnit = MutableStateFlow(repository.getSavedPrecipitationUnit())
    val precipUnit = _precipUnit.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled = _notificationsEnabled.asStateFlow()

    private val _statusBarEnabled = MutableStateFlow(true)
    val statusBarEnabled = _statusBarEnabled.asStateFlow()

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

    fun updateSettings(
        temp: String? = null,
        time: String? = null,
        wind: String? = null,
        pressure: String? = null,
        precipitation: String? = null
    ) {
        viewModelScope.launch {
            temp?.let {
                repository.saveInitialSetup(it, _timeFormat.value, _windUnit.value)
                _tempUnit.value = it
            }
            wind?.let {
                repository.saveInitialSetup(_tempUnit.value, _timeFormat.value, it)
                _windUnit.value = it
            }
            time?.let {
                val normalizedTime = if (it.contains("12")) "12h" else "24h"
                repository.saveInitialSetup(_tempUnit.value, normalizedTime, _windUnit.value)
                _timeFormat.value = normalizedTime
            }
            pressure?.let { 
                repository.savePressureUnit(it)
                _pressureUnit.value = it
            }
            precipitation?.let { 
                repository.savePrecipitationUnit(it)
                _precipUnit.value = it
            }

            _locationState.value?.let { fetchWeather(it.latitude, it.longitude, _addressState.value) }
        }
    }

    fun toggleNotifications(enabled: Boolean) { _notificationsEnabled.value = enabled }
    fun toggleStatusBar(enabled: Boolean) { _statusBarEnabled.value = enabled }

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
                    val addressText = "${address.thoroughfare ?: context.getString(R.string.unknown_street)}, ${address.locality ?: context.getString(R.string.unknown_city)}"
                    withContext(Dispatchers.Main) { _addressState.value = addressText }
                    addressText
                } else {
                    val fallback = context.getString(R.string.address_not_found)
                    withContext(Dispatchers.Main) { _addressState.value = fallback }
                    fallback
                }
            } catch (e: Exception) {
                val fallback = context.getString(R.string.address_not_found)
                withContext(Dispatchers.Main) { _addressState.value = fallback }
                fallback
            }
        }
    }

    fun onSetupDoneClicked(tempUnit: String, timeFormat: String, windUnit: String) {
        viewModelScope.launch {
            val normalizedTime = if (timeFormat.contains("12")) "12h" else "24h"
            repository.saveInitialSetup(tempUnit, normalizedTime, windUnit)
            _tempUnit.value = tempUnit
            _timeFormat.value = normalizedTime
            _windUnit.value = windUnit
            _uiState.value = WeatherUiState.Loading
            _eventFlow.emit(WeatherEvent.SetupCompleted)
            checkStatusAndFetch(false, true, true)
        }
    }

    fun onMenuClicked() { viewModelScope.launch { _eventFlow.emit(WeatherEvent.OpenNavigationDrawer) } }
    fun onPageIndicatorClicked(pageIndex: Int) { viewModelScope.launch { _eventFlow.emit(WeatherEvent.ScrollToPage(pageIndex)) } }

    fun checkStatusAndFetch(isPermissionGranted: Boolean, isNetworkAvailable: Boolean, isGpsEnabled: Boolean) {
        viewModelScope.launch {
            if (!isPermissionGranted) {
                isSplashLoading.value = false
                _eventFlow.emit(WeatherEvent.RequestLocationPermission)
                return@launch
            }
            if (!isNetworkAvailable) {
                isSplashLoading.value = false
                _uiState.value = WeatherUiState.Error(context.getString(R.string.no_internet_error))
                return@launch
            }
            if (!isGpsEnabled) {
                isSplashLoading.value = false
                _uiState.value = WeatherUiState.Error(context.getString(R.string.enable_gps_error))
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
            val pressureUnit = repository.getSavedPressureUnit()
            val precipUnit = repository.getSavedPrecipitationUnit()

            repository.getHomeWeather(lat, lon, BuildConfig.API_KEY)
                .onSuccess { data ->
                    _uiState.value = WeatherUiState.Success(
                        data = processWeatherData(data),
                        unit = unitSymbol,
                        timeFormat = timeFormat,
                        windUnit = windUnit,
                        pressureUnit = pressureUnit,
                        precipUnit = precipUnit,
                        address = address
                    )
                    isSplashLoading.value = false
                }.onFailure {
                    _uiState.value = WeatherUiState.Error(context.getString(R.string.failed_load_weather))
                    isSplashLoading.value = false
                }
        }
    }

    private fun processWeatherData(data: FullWeatherData): FullWeatherData {
        val windUnit = repository.getSavedWindUnit()
        val pressureUnit = repository.getSavedPressureUnit()
        val tempUnit = repository.getUserUnitSymbol()

        val updatedCurrent = data.current.copy(
            wind = data.current.wind.copy(
                speed = UnitConverter.convertWindSpeed(data.current.wind.speed, tempUnit, windUnit).toDouble()
            ),
            main = data.current.main.copy(
                pressure = UnitConverter.convertPressure(data.current.main.pressure, pressureUnit).toDouble().toInt()
            )
        )

        val updatedForecastList = data.forecast.list.map { item ->
            item.copy(
                wind = item.wind.copy(
                    speed = UnitConverter.convertWindSpeed(item.wind.speed, tempUnit, windUnit).toDouble()
                ),
                main = item.main.copy(
                    pressure = UnitConverter.convertPressure(item.main.pressure, pressureUnit).toDouble().toInt()
                )
            )
        }

        val hourly = updatedForecastList.take(8)
        val daily = updatedForecastList.filter { it.dtTxt.contains("12:00:00") }

        return data.copy(
            current = updatedCurrent,
            forecast = data.forecast.copy(list = hourly + daily)
        )
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
