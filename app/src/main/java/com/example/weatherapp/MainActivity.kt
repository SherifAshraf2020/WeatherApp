package com.example.weatherapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
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

        setContent {
            WeatherAppTheme {
                val repository = WeatherRepository(
                    WeatherRemoteDataSource(),
                    PreferenceManager(applicationContext)
                )
                val factory = WeatherViewModelFactory(repository)
                val viewModel: WeatherViewModel = viewModel(factory = factory)

                splashScreen.setKeepOnScreenCondition {
                    viewModel.uiState.value == WeatherUiState.Loading
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



