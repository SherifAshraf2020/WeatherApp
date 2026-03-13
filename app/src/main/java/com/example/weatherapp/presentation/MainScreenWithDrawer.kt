package com.example.weatherapp.presentation

import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.weatherapp.R
import com.example.weatherapp.presentation.home.CurrentWeatherScreen
import com.example.weatherapp.presentation.home.WeatherEvent
import com.example.weatherapp.presentation.home.WeatherUiState
import com.example.weatherapp.presentation.home.WeatherViewModel
import com.example.weatherapp.presentation.favorites.FavoritesViewModel
import com.example.weatherapp.presentation.favorites.FavoritesScreen
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MainScreenWithDrawer(
    viewModel: WeatherViewModel,
    favoritesViewModel: FavoritesViewModel,
    navController: NavHostController
) {
    val uiState by viewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 3 })

    var showUnitDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }

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
            ModalDrawerSheet(
                modifier = Modifier.fillMaxWidth(0.8f),
                drawerContainerColor = Color(0xFF212121),
                drawerShape = RoundedCornerShape(0.dp)
            ) {
                DrawerMenuContent(
                    currentPage = pagerState.currentPage,
                    onItemClick = { page ->
                        scope.launch {
                            pagerState.animateScrollToPage(page)
                            drawerState.close()
                        }
                    },
                    onUnitSettingsClick = {
                        scope.launch { drawerState.close() }
                        showUnitDialog = true
                    },
                    onLanguageClick = {
                        scope.launch { drawerState.close() }
                        showLanguageDialog = true
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Black.copy(alpha = 0.2f)
                    ),
                    navigationIcon = {
                        IconButton(onClick = { viewModel.onMenuClicked() }) {
                            Icon(
                                Icons.Default.Menu,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                    when (page) {
                        0 -> WeatherLogicContainer(uiState, viewModel)
                        1 -> FavoritesScreen(
                            viewModel = favoritesViewModel,
                            onNavigateToMap = { navController.navigate("map_screen") },
                            onNavigateToDetails = { lat, lon, city ->
                                navController.navigate("weather_details/$lat/$lon/$city")
                            }
                        )
                        2 -> AlertScreen()
                    }
                }

                // Pager Indicators
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(pagerState.pageCount) { iteration ->
                            val isSelected = pagerState.currentPage == iteration
                            Box(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .size(if (isSelected) 10.dp else 8.dp)
                                    .background(
                                        if (isSelected) Color(0xFF00ACC1) else Color.White.copy(alpha = 0.6f),
                                        CircleShape
                                    )
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

    if (showUnitDialog) {
        UnitSettingsDialog(onDismiss = { showUnitDialog = false })
    }
    if (showLanguageDialog) {
        LanguageSelectionDialog(onDismiss = { showLanguageDialog = false })
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WeatherLogicContainer(state: WeatherUiState, viewModel: WeatherViewModel) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (state) {
            is WeatherUiState.Loading -> { CircularProgressIndicator() }
            is WeatherUiState.PermissionRequired -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.LocationOff, null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                    Text(stringResource(id = R.string.permission_needed), modifier = Modifier.padding(16.dp), color = Color.White)
                    Button(onClick = { viewModel.startGettingLocation() }) {
                        Text(stringResource(id = R.string.btn_grant_permission))
                    }
                }
            }
            is WeatherUiState.Success -> {
                CurrentWeatherScreen(
                    data = state.data,
                    unit = state.unit,
                    timeFormat = state.timeFormat,
                    windUnit = state.windUnit,
                    address = state.address
                )
            }
            is WeatherUiState.Error -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CloudOff, null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                    Text("${stringResource(id = R.string.error_prefix)} ${state.message}", color = Color.Red, modifier = Modifier.padding(16.dp))
                    Button(onClick = { viewModel.startGettingLocation() }) {
                        Text(stringResource(id = R.string.btn_retry))
                    }
                }
            }
            else -> { Text(stringResource(id = R.string.setup_incomplete), color = Color.White) }
        }
    }
}

@Composable
fun AlertScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.NotificationsNone, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
            Text(stringResource(id = R.string.no_alerts_text), style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
        }
    }
}
