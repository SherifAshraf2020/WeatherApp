package com.example.weatherapp

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherapp.data.FusedLocationHelper
import com.example.weatherapp.data.WeatherRepository
import com.example.weatherapp.data.datasource.local.PreferenceManager
import com.example.weatherapp.data.datasource.remote.WeatherRemoteDataSource
import com.example.weatherapp.presentation.*
import com.example.weatherapp.ui.theme.WeatherAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. تجهيز الـ Helper والـ Repository
        val locationHelper = FusedLocationHelper(applicationContext)
        val repository = WeatherRepository(
            WeatherRemoteDataSource(),
            PreferenceManager(applicationContext)
        )

        // 2. تحديث الـ Factory بالباراميترز الجديدة
        val factory = WeatherViewModelFactory(repository, locationHelper, applicationContext)

        setContent {
            WeatherAppTheme {
                val viewModel: WeatherViewModel = viewModel(factory = factory)

                // 3. الـ Launcher لطلب الصلاحيات (Location Permission)
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { permissions ->
                    val isGranted = permissions.values.any { it }
                    viewModel.checkStatusAndFetch(
                        isPermissionGranted = isGranted,
                        isNetworkAvailable = isNetworkAvailable(),
                        isGpsEnabled = locationHelper.isLocationEnabled()
                    )
                }

                androidx.compose.runtime.LaunchedEffect(Unit) {
                    // السطر ده هو "المايسترو" اللي بيبدأ كل حاجة صح
                    val isGranted = checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED

                    viewModel.checkStatusAndFetch(
                        isPermissionGranted = isGranted,
                        isNetworkAvailable = isNetworkAvailable(),
                        isGpsEnabled = locationHelper.isLocationEnabled()
                    )

                    viewModel.eventFlow.collect { event ->
                        when (event) {
                            is WeatherEvent.RequestLocationPermission -> {
                                permissionLauncher.launch(
                                    arrayOf(
                                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                            is WeatherEvent.GpsNotEnabled -> {
                                android.widget.Toast.makeText(
                                    this@MainActivity,
                                    "Please enable GPS",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            }
                            is WeatherEvent.NetworkNotFound -> {
                                android.widget.Toast.makeText(
                                    this@MainActivity,
                                    "No Internet Connection",
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                            }
                            else -> {}
                        }
                    }
                }

                splashScreen.setKeepOnScreenCondition {
                    viewModel.isSplashLoading.value
                }

                val uiState by viewModel.uiState.collectAsState()

                when (uiState) {
                    is WeatherUiState.SetupRequired -> {
                        InitialSetupScreen(viewModel)
                    }

                    else -> {
                        MainScreenWithDrawer(viewModel)
                    }
                }
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



