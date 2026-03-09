package com.example.weatherapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
                val permissionLauncher =
                    androidx.activity.compose.rememberLauncherForActivityResult(
                        contract = androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
                    ) { permissions ->
                        val isGranted = permissions.values.any { it }
                        // بنبلغ الـ ViewModel بالنتيجة وهو يتصرف
                        viewModel.checkStatusAndFetch(
                            isPermissionGranted = isGranted,
                            isNetworkAvailable = true, // ممكن نضيف فحص النت لاحقاً
                            isGpsEnabled = locationHelper.isLocationEnabled()
                        )
                    }

                // 4. مراقبة الـ Events اللحظية (طلب الصلاحيات، الـ GPS مقفول، إلخ)
                androidx.compose.runtime.LaunchedEffect(Unit) {
                    if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                        viewModel.startGettingLocation()
                    }
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
}



