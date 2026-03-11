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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.weatherapp.R
import com.example.weatherapp.presentation.home.CurrentWeatherScreen
import com.example.weatherapp.presentation.home.WeatherEvent
import com.example.weatherapp.presentation.home.WeatherUiState
import com.example.weatherapp.presentation.home.WeatherViewModel
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MainScreenWithDrawer(viewModel: WeatherViewModel) {
    val uiState by viewModel.uiState.collectAsState()
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
                    text = stringResource(id = R.string.app_name),
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                HorizontalDivider()
                NavigationDrawerItem(
                    label = { Text(stringResource(id = R.string.menu_home)) },
                    selected = pagerState.currentPage == 0,
                    onClick = { scope.launch { pagerState.animateScrollToPage(0); drawerState.close() } },
                    icon = { Icon(Icons.Default.Home, null) }
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(id = R.string.favorite_locations)) },
                    selected = pagerState.currentPage == 1,
                    onClick = { scope.launch { pagerState.animateScrollToPage(1); drawerState.close() } },
                    icon = { Icon(Icons.Default.Favorite, null) }
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(id = R.string.menu_alerts)) },
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
                    title = { },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    ),
                    navigationIcon = {
                        IconButton(onClick = { viewModel.onMenuClicked() }) {
                            Icon(Icons.Default.Menu, contentDescription = stringResource(id = R.string.menu_description))
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

@RequiresApi(Build.VERSION_CODES.O)
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
                    Text(stringResource(id = R.string.permission_needed), modifier = Modifier.padding(16.dp))
                    Button(onClick = { viewModel.startGettingLocation() }) {
                        Text(stringResource(id = R.string.btn_grant_permission))
                    }
                }
            }
            is WeatherUiState.Success -> {
                CurrentWeatherScreen(data = state.data, unit = state.unit)
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
            else -> {
                Text(stringResource(id = R.string.setup_incomplete))
            }
        }
    }
}

@Composable
fun FavoriteScreen() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(stringResource(id = R.string.favorite_locations), style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))
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
            Text(stringResource(id = R.string.no_alerts_text), style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
        }
    }
}