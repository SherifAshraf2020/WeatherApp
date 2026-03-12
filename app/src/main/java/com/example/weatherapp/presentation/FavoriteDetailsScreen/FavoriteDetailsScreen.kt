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
import com.example.weatherapp.BuildConfig
import com.example.weatherapp.data.models.home.FullWeatherData
import com.example.weatherapp.data.repository.WeatherRepository
import com.example.weatherapp.presentation.home.CurrentWeatherScreen

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
    var weatherData by remember { mutableStateOf<FullWeatherData?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(lat, lon) {
        repository.getHomeWeather(lat, lon, BuildConfig.API_KEY)
            .onSuccess { data ->
                weatherData = data
                isLoading = false
            }
            .onFailure {
                errorMessage = "Error"
                isLoading = false
            }
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
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (errorMessage != null) {
                Text(text = errorMessage!!, modifier = Modifier.align(Alignment.Center), color = Color.Red)
            } else {
                weatherData?.let { data ->
                    CurrentWeatherScreen(data = data, unit = repository.getUserUnitSymbol())
                }
            }
        }
    }
}