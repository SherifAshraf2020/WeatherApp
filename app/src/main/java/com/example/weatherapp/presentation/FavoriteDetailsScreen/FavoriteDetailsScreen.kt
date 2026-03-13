package com.example.weatherapp.presentation.FavoriteDetailsScreen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherapp.data.repository.WeatherRepository
import com.example.weatherapp.presentation.home.CurrentWeatherScreen
import com.example.weatherapp.presentation.home.WeatherUiState

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteDetailsScreen(
    lat: Double,
    lon: Double,
    city: String,
    repository: WeatherRepository,
    onBack: () -> Unit
) {
    val context = LocalContext.current.applicationContext
    val viewModel: FavoriteDetailsViewModel = viewModel(
        factory = FavoriteDetailsViewModel.Factory(repository, context)
    )

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(lat, lon) {
        viewModel.fetchWeather(lat, lon)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = city, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is WeatherUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is WeatherUiState.Error -> {
                    Text(
                        text = state.message,
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.Red
                    )
                }
                is WeatherUiState.Success -> {
                    CurrentWeatherScreen(
                        data = state.data,
                        unit = state.unit,
                        timeFormat = state.timeFormat,
                        windUnit = state.windUnit
                    )
                }
                else -> {}
            }
        }
    }
}
