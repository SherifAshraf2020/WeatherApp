package com.example.weatherapp.presentation.favorites

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.weatherapp.R
import com.example.weatherapp.data.datasource.local.entities.FavoriteEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    viewModel: FavoritesViewModel,
    onNavigateToMap: () -> Unit,
    onNavigateToDetails: (Double, Double, String) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // 1. Handling Side Effects (Events)
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is FavoritesUiEvent.NavigateToMap -> onNavigateToMap()
                is FavoritesUiEvent.NavigateToWeatherDetails -> {
                    onNavigateToDetails(event.lat, event.lon, event.cityName)
                }
                is FavoritesUiEvent.ShowToast -> {
                    val message = context.getString(R.string.removed_message, event.message)
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()                }
            }
        }
    }

    // 2. Main Layout
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.favorite_locations)) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.onAddLocationClicked() },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(id = R.string.add_location)
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when (val currentState = state) {
                is FavoritesUiState.Loading -> {
                    CircularProgressIndicator()
                }
                is FavoritesUiState.Success -> {
                    if (currentState.favorites.isEmpty()) {
                        Text(text = stringResource(id = R.string.no_favorites_found))
                    } else {
                        FavoritesList(
                            favorites = currentState.favorites,
                            onItemClick = { viewModel.onLocationSelected(it) },
                            onDeleteClick = { viewModel.onDeleteLocation(it) }
                        )
                    }
                }
                is FavoritesUiState.Error -> {
                    Text(
                        text = currentState.message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun FavoritesList(
    favorites: List<FavoriteEntity>,
    onItemClick: (FavoriteEntity) -> Unit,
    onDeleteClick: (FavoriteEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(favorites) { favorite ->
            FavoriteItem(
                favorite = favorite,
                onItemClick = onItemClick,
                onDeleteClick = onDeleteClick
            )
        }
    }
}

@Composable
fun FavoriteItem(
    favorite: FavoriteEntity,
    onItemClick: (FavoriteEntity) -> Unit,
    onDeleteClick: (FavoriteEntity) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick(favorite) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = favorite.cityName,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${favorite.latitude}, ${favorite.longitude}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            IconButton(onClick = { onDeleteClick(favorite) }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(id = R.string.delete_location),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}