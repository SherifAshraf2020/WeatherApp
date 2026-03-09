package com.example.weatherapp.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weatherapp.data.models.home.FullWeatherData
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MainScreenWithDrawer(viewModel: WeatherViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val address by viewModel.addressState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val pagerState = rememberPagerState(pageCount = { 3 })

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            if (event is WeatherEvent.OpenNavigationDrawer) {
                scope.launch { drawerState.open() }
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Weather App",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                HorizontalDivider()
                NavigationDrawerItem(
                    label = { Text("Home") },
                    selected = pagerState.currentPage == 0,
                    onClick = { scope.launch { pagerState.animateScrollToPage(0); drawerState.close() } },
                    icon = { Icon(Icons.Default.Home, null) }
                )
                NavigationDrawerItem(
                    label = { Text("Favorites") },
                    selected = pagerState.currentPage == 1,
                    onClick = { scope.launch { pagerState.animateScrollToPage(1); drawerState.close() } },
                    icon = { Icon(Icons.Default.Favorite, null) }
                )
                NavigationDrawerItem(
                    label = { Text("Alerts") },
                    selected = pagerState.currentPage == 2,
                    onClick = { scope.launch { pagerState.animateScrollToPage(2); drawerState.close() } },
                    icon = { Icon(Icons.Default.Notifications, null) }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = address.ifEmpty { "Detecting Location..." },
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text("Current Location", style = MaterialTheme.typography.labelSmall)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.onMenuClicked() }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) { page ->
                    when (page) {
                        0 -> WeatherLogicContainer(uiState, viewModel)
                        1 -> FavoriteScreen()
                        2 -> AlertScreen()
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(pagerState.pageCount) { iteration ->
                            val isSelected = pagerState.currentPage == iteration
                            val color = if (isSelected)
                                MaterialTheme.colorScheme.primary else Color.LightGray

                            Box(
                                modifier = Modifier
                                    .padding(6.dp)
                                    .size(if (isSelected) 12.dp else 8.dp)
                                    .background(color, shape = CircleShape)
                                    .clickable {
                                        scope.launch { pagerState.animateScrollToPage(iteration) }
                                    }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WeatherLogicContainer(state: WeatherUiState, viewModel: WeatherViewModel) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (state) {
            is WeatherUiState.Loading -> {
                CircularProgressIndicator()
            }
            is WeatherUiState.PermissionRequired -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.LocationOff, null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                    Text("Location Permission Needed", modifier = Modifier.padding(16.dp))
                    Button(onClick = { viewModel.startGettingLocation() }) {
                        Text("Grant Permission")
                    }
                }
            }
            is WeatherUiState.Success -> {
                CurrentWeatherScreen(state.data)
            }
            is WeatherUiState.Error -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CloudOff, null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                    Text("Error: ${state.message}", color = Color.Red, modifier = Modifier.padding(16.dp))
                    Button(onClick = { viewModel.startGettingLocation() }) {
                        Text("Retry")
                    }
                }
            }
            else -> {
                Text("Please complete the setup or check permissions")
            }
        }
    }
}


@Composable
fun CurrentWeatherScreen(data: FullWeatherData) {
    val current = data.current
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(32.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.WbSunny, null, modifier = Modifier.size(80.dp), tint = Color(0xFFFFC107))
                Spacer(modifier = Modifier.height(8.dp))
                Text("${current.main.temp.toInt()}°C", fontSize = 72.sp, fontWeight = FontWeight.Bold)
                Text(current.weather.firstOrNull()?.main ?: "Unknown", style = MaterialTheme.typography.headlineSmall)
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            WeatherDetailBox("Wind", "${current.wind.speed} km/h", Icons.Default.Air)
            WeatherDetailBox("Humidity", "${current.main.humidity}%", Icons.Default.WaterDrop)
        }
    }
}

@Composable
fun WeatherDetailBox(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
        Text(value, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
    }
}

@Composable
fun FavoriteScreen() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Favorite Locations", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))
        // Static Examples
        listOf("London", "New York", "Tokyo").forEach { city ->
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                ListItem(
                    headlineContent = { Text(city) },
                    trailingContent = { Text("22°C", fontWeight = FontWeight.Bold) },
                    leadingContent = { Icon(Icons.Default.LocationOn, null) }
                )
            }
        }
    }
}

@Composable
fun AlertScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.NotificationsNone, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
            Text("No Active Alerts", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
        }
    }
}