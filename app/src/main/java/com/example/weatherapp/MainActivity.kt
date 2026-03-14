package com.example.weatherapp

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.weatherapp.data.datasource.location.FusedLocationHelper
import com.example.weatherapp.data.datasource.local.PreferenceManager
import com.example.weatherapp.data.datasource.local.WeatherLocalDataSource
import com.example.weatherapp.data.datasource.remote.WeatherRemoteDataSource
import com.example.weatherapp.data.db.WeatherDatabase
import com.example.weatherapp.data.repository.WeatherRepository
import com.example.weatherapp.data.util.LocaleHelper
import com.example.weatherapp.presentation.*
import com.example.weatherapp.presentation.FavoriteDetailsScreen.FavoriteDetailsScreen
import com.example.weatherapp.presentation.home.*
import com.example.weatherapp.presentation.favorites.FavoritesViewModel
import com.example.weatherapp.presentation.favorites.FavoritesViewModelFactory
import com.example.weatherapp.presentation.map.MapScreen
import com.example.weatherapp.presentation.map.MapViewModel
import com.example.weatherapp.presentation.map.MapViewModelFactory
import com.example.weatherapp.ui.theme.WeatherAppTheme
import org.osmdroid.config.Configuration

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        val preferenceManager = PreferenceManager(applicationContext)
        val currentLang = preferenceManager.getLanguage() ?: "en"
        LocaleHelper.applyLocale(this, currentLang)

        super.onCreate(savedInstanceState)

        Configuration.getInstance().load(
            this,
            android.preference.PreferenceManager.getDefaultSharedPreferences(this)
        )

        enableEdgeToEdge()

        val locationHelper = FusedLocationHelper(applicationContext)
        val database = WeatherDatabase.getDatabase(applicationContext)
        val localDataSource = WeatherLocalDataSource(database.favoriteDao())

        val repository = WeatherRepository(
            remoteDataSource = WeatherRemoteDataSource(),
            localDataSource = localDataSource,
            preferenceManager = preferenceManager
        )

        val weatherFactory = WeatherViewModelFactory(repository, locationHelper, applicationContext)
        val favoritesFactory = FavoritesViewModelFactory(repository)

        setContent {
            WeatherAppTheme {
                val navController = rememberNavController()
                val weatherViewModel: WeatherViewModel = viewModel(factory = weatherFactory)
                val favoritesViewModel: FavoritesViewModel = viewModel(factory = favoritesFactory)

                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { permissions ->
                    val isGranted = permissions.values.any { it }
                    weatherViewModel.checkStatusAndFetch(
                        isPermissionGranted = isGranted,
                        isNetworkAvailable = isNetworkAvailable(),
                        isGpsEnabled = locationHelper.isLocationEnabled()
                    )
                }

                LaunchedEffect(Unit) {
                    val isGranted = checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
                    weatherViewModel.checkStatusAndFetch(
                        isPermissionGranted = isGranted,
                        isNetworkAvailable = isNetworkAvailable(),
                        isGpsEnabled = locationHelper.isLocationEnabled()
                    )
                    weatherViewModel.eventFlow.collect { event ->
                        when (event) {
                            is WeatherEvent.RequestLocationPermission -> {
                                permissionLauncher.launch(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION))
                            }
                            is WeatherEvent.GpsNotEnabled -> {
                                requestEnableGps()
                            }
                            is WeatherEvent.NetworkNotFound -> {
                                Toast.makeText(this@MainActivity, getString(R.string.no_internet_error), Toast.LENGTH_LONG).show()
                            }
                            is WeatherEvent.LanguageChanged -> {
                                recreate()
                            }
                            else -> {}
                        }
                    }
                }

                splashScreen.setKeepOnScreenCondition { weatherViewModel.isSplashLoading.value }

                NavHost(navController = navController, startDestination = "home") {
                    composable("home") {
                        val uiState by weatherViewModel.uiState.collectAsState()
                        when (uiState) {
                            is WeatherUiState.SetupRequired -> InitialSetupScreen(weatherViewModel)
                            else -> MainScreenWithDrawer(weatherViewModel, favoritesViewModel, navController)
                        }
                    }

                    composable("weather_details/{lat}/{lon}/{city}") { backStackEntry ->
                        val lat = backStackEntry.arguments?.getString("lat")?.toDoubleOrNull() ?: 0.0
                        val lon = backStackEntry.arguments?.getString("lon")?.toDoubleOrNull() ?: 0.0
                        val city = backStackEntry.arguments?.getString("city") ?: ""

                        FavoriteDetailsScreen(lat, lon, city, repository, onBack = { navController.popBackStack() })
                    }

                    composable("map_screen") {
                        val mapViewModel: MapViewModel = viewModel(factory = MapViewModelFactory(repository))
                        MapScreen(viewModel = mapViewModel, onLocationSaved = { navController.popBackStack() })
                    }
                }
            }
        }
    }

    private fun requestEnableGps() {
        val locationRequest = com.google.android.gms.location.LocationRequest.Builder(com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, 5000).build()
        val builder = com.google.android.gms.location.LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client = com.google.android.gms.location.LocationServices.getSettingsClient(this)
        client.checkLocationSettings(builder.build()).addOnFailureListener { exception ->
            if (exception is com.google.android.gms.common.api.ResolvableApiException) {
                try { exception.startResolutionForResult(this, 100) } catch (e: Exception) {}
            }
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}