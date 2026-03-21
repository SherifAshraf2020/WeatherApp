package com.example.weatherapp.presentation.home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
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
import kotlinx.coroutines.flow.firstOrNull
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

    private val _notificationsEnabled = MutableStateFlow(repository.isNotificationsEnabled())
    val notificationsEnabled = _notificationsEnabled.asStateFlow()

    private val _statusBarEnabled = MutableStateFlow(repository.isStatusBarEnabled())
    val statusBarEnabled = _statusBarEnabled.asStateFlow()

    private val _locationState = MutableStateFlow<Location?>(null)
    val locationState: StateFlow<Location?> = _locationState.asStateFlow()

    private val _addressState = MutableStateFlow(context.getString(R.string.waiting))
    val addressState: StateFlow<String> = _addressState.asStateFlow()

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState

    private val _eventFlow = MutableSharedFlow<WeatherEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private val _language = MutableStateFlow(repository.getSavedLanguage())
    val language = _language.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    var isSplashLoading = mutableStateOf(true)
        private set

    init {
        checkAppStartStatus()
        observeHomeWeather()
    }

    private fun observeHomeWeather() {
        viewModelScope.launch {
            repository.getHomeWeatherFromLocal().collect { data ->
                if (data != null) {
                    val unitSymbol = repository.getUserUnitSymbol()
                    val timeFormat = repository.getSavedTimeFormat()
                    val windUnit = repository.getSavedWindUnit()
                    val pressureUnit = repository.getSavedPressureUnit()
                    val precipUnit = repository.getSavedPrecipitationUnit()

                    // Restore address from database if it exists
                    if (data.address.isNotEmpty() && data.address != context.getString(R.string.waiting)) {
                        _addressState.value = data.address
                    }

                    _uiState.value = WeatherUiState.Success(
                        data = processWeatherData(data),
                        unit = unitSymbol,
                        timeFormat = timeFormat,
                        windUnit = windUnit,
                        pressureUnit = pressureUnit,
                        precipUnit = precipUnit,
                        address = data.address.ifEmpty { _addressState.value }
                    )
                    isSplashLoading.value = false
                }
            }
        }
    }

    private fun checkAppStartStatus() {
        if (repository.isFirstTimeUser()) {
            _uiState.value = WeatherUiState.SetupRequired
            isSplashLoading.value = false
        } else {
            viewModelScope.launch {
                val cachedData = repository.getHomeWeatherFromLocal().firstOrNull()
                if (cachedData != null) {
                    if (cachedData.address.isNotEmpty() && cachedData.address != context.getString(R.string.waiting)) {
                        _addressState.value = cachedData.address
                    }
                    isSplashLoading.value = false
                } else {
                    _eventFlow.emit(WeatherEvent.RequestLocationPermission)
                }
            }
        }
    }

    private fun isNetworkAvailableInternal(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun refresh() {
        viewModelScope.launch {
            if (_isRefreshing.value) return@launch
            
            if (!isNetworkAvailableInternal()) {
                _eventFlow.emit(WeatherEvent.NetworkNotFound)
                return@launch
            }

            _isRefreshing.value = true
            locationHelper.getFreshLocation { location ->
                if (location != null) {
                    _locationState.value = location
                    viewModelScope.launch {
                        val address = updateAddress(location)
                        updateWeatherFromRemote(location.latitude, location.longitude, address)
                    }
                } else {
                    _isRefreshing.value = false
                    val lastLocation = _locationState.value
                    if (lastLocation != null) {
                        viewModelScope.launch {
                            val address = if (_addressState.value == context.getString(R.string.waiting)) "" else _addressState.value
                            updateWeatherFromRemote(lastLocation.latitude, lastLocation.longitude, address)
                        }
                    } else {
                        if (_uiState.value !is WeatherUiState.Success) {
                            _uiState.value = WeatherUiState.Error(context.getString(R.string.enable_gps_error))
                            viewModelScope.launch { _eventFlow.emit(WeatherEvent.GpsNotEnabled) }
                        }
                    }
                }
            }
        }
    }

    private fun updateWeatherFromRemote(lat: Double, lon: Double, address: String) {
        viewModelScope.launch {
            repository.refreshHomeWeather(lat, lon, BuildConfig.API_KEY, address)
                .onSuccess {
                    _isRefreshing.value = false
                }
                .onFailure {
                    _isRefreshing.value = false
                    _eventFlow.emit(WeatherEvent.NetworkNotFound)
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

            _locationState.value?.let { 
                val address = if (_addressState.value == context.getString(R.string.waiting)) "" else _addressState.value
                fetchWeather(it.latitude, it.longitude, address) 
            }
        }
    }

    fun changeLanguage(langCode: String) {
        viewModelScope.launch {
            repository.saveLanguage(langCode)
            _language.value = langCode
            _eventFlow.emit(WeatherEvent.LanguageChanged)
            _locationState.value?.let {
                val address = updateAddress(it)
                fetchWeather(it.latitude, it.longitude, address)
            }
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                viewModelScope.launch {
                    _eventFlow.emit(WeatherEvent.RequestNotificationPermission)
                }
                return
            }
        }
        _notificationsEnabled.value = enabled
        repository.setNotificationsEnabled(enabled)
    }

    fun toggleStatusBar(enabled: Boolean) {
        _statusBarEnabled.value = enabled
        repository.setStatusBarEnabled(enabled)
    }

    fun startGettingLocation() {
        if (!isNetworkAvailableInternal()) {
            _uiState.value = WeatherUiState.Error(context.getString(R.string.no_internet_error))
            viewModelScope.launch { _eventFlow.emit(WeatherEvent.NetworkNotFound) }
            return
        }

        if (!locationHelper.isLocationEnabled()) {
            _uiState.value = WeatherUiState.Error(context.getString(R.string.enable_gps_error))
            viewModelScope.launch { _eventFlow.emit(WeatherEvent.GpsNotEnabled) }
            return
        }

        if (_uiState.value !is WeatherUiState.Success) {
            _uiState.value = WeatherUiState.Loading
        }
        
        locationHelper.getFreshLocation { location ->
            location?.let {
                _locationState.value = it
                viewModelScope.launch {
                    val address = updateAddress(it)
                    fetchWeather(it.latitude, it.longitude, address)
                }
            } ?: run {
                isSplashLoading.value = false
                if (_uiState.value !is WeatherUiState.Success) {
                    _uiState.value = WeatherUiState.Error(context.getString(R.string.enable_gps_error))
                    viewModelScope.launch { _eventFlow.emit(WeatherEvent.GpsNotEnabled) }
                }
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
                    withContext(Dispatchers.Main) { 
                        if (_addressState.value == context.getString(R.string.waiting)) {
                            _addressState.value = fallback 
                        }
                    }
                    fallback
                }
            } catch (e: Exception) {
                val fallback = context.getString(R.string.address_not_found)
                withContext(Dispatchers.Main) { 
                    if (_addressState.value == context.getString(R.string.waiting)) {
                        _addressState.value = fallback 
                    }
                }
                fallback
            }
        }
    }

    fun onSetupDoneClicked(
        tempUnit: String,
        timeFormat: String,
        windUnit: String,
        notificationsEnabled: Boolean,
        statusBarEnabled: Boolean
    ) {
        viewModelScope.launch {
            val normalizedTime = if (timeFormat.contains("12")) "12h" else "24h"
            repository.saveInitialSetup(tempUnit, normalizedTime, windUnit)
            repository.setNotificationsEnabled(notificationsEnabled)
            repository.setStatusBarEnabled(statusBarEnabled)

            _tempUnit.value = tempUnit
            _timeFormat.value = normalizedTime
            _windUnit.value = windUnit
            _notificationsEnabled.value = notificationsEnabled
            _statusBarEnabled.value = statusBarEnabled

            _uiState.value = WeatherUiState.Loading
            _eventFlow.emit(WeatherEvent.SetupCompleted)
            checkStatusAndFetch()
        }
    }

    fun onMenuClicked() { viewModelScope.launch { _eventFlow.emit(WeatherEvent.OpenNavigationDrawer) } }
    fun onPageIndicatorClicked(pageIndex: Int) { viewModelScope.launch { _eventFlow.emit(WeatherEvent.ScrollToPage(pageIndex)) } }

    fun checkStatusAndFetch(
        isPermissionGranted: Boolean = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED,
        isNetworkAvailable: Boolean? = null,
        isGpsEnabled: Boolean? = null
    ) {
        // Force re-checking current status instead of relying on passed parameters
        val network = isNetworkAvailableInternal()
        val gps = locationHelper.isLocationEnabled()

        viewModelScope.launch {
            if (!isPermissionGranted) {
                isSplashLoading.value = false
                _eventFlow.emit(WeatherEvent.RequestLocationPermission)
                return@launch
            }

            if (!network) {
                isSplashLoading.value = false
                if (_uiState.value is WeatherUiState.Success) {
                    _eventFlow.emit(WeatherEvent.NetworkNotFound)
                } else {
                    _uiState.value = WeatherUiState.Error(context.getString(R.string.no_internet_error))
                    _eventFlow.emit(WeatherEvent.NetworkNotFound)
                }
                return@launch
            }

            if (!gps) {
                isSplashLoading.value = false
                if (_uiState.value !is WeatherUiState.Success) {
                    _uiState.value = WeatherUiState.Error(context.getString(R.string.enable_gps_error))
                    _eventFlow.emit(WeatherEvent.GpsNotEnabled)
                }
                return@launch
            }

            if (_uiState.value !is WeatherUiState.Success) {
                _uiState.value = WeatherUiState.Loading
            }
            startGettingLocation()
        }
    }

    private fun fetchWeather(lat: Double, lon: Double, address: String = "") {
        viewModelScope.launch {
            repository.saveHomeLocation(lat, lon)
            repository.refreshHomeWeather(lat, lon, BuildConfig.API_KEY, address)
                .onFailure {
                    if (_uiState.value !is WeatherUiState.Success) {
                        _uiState.value = WeatherUiState.Error(context.getString(R.string.failed_load_weather))
                        _eventFlow.emit(WeatherEvent.NetworkNotFound)
                    }
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
