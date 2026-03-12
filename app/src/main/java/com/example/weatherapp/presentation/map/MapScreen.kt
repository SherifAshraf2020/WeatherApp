package com.example.weatherapp.presentation.map

import android.location.Geocoder
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.weatherapp.R
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import java.util.Locale

@Composable
fun MapScreen(
    viewModel: MapViewModel,
    onLocationSaved: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val mapView = remember { MapView(context) }
    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }
    var locationName by remember { mutableStateOf<String?>(null) }
    val marker = remember { Marker(mapView) }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is MapUiEvent.LocationSaved -> onLocationSaved()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = {
                mapView.apply {
                    setMultiTouchControls(true)
                    setBuiltInZoomControls(true)
                    zoomController.setVisibility(org.osmdroid.views.CustomZoomButtonsController.Visibility.ALWAYS)

                    controller.setZoom(10.0)
                    controller.setCenter(GeoPoint(30.0444, 31.2357))
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 50.dp),
            update = { view ->
                val eventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
                    override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                        selectedLocation = LatLng(p.latitude, p.longitude)
                        marker.position = p
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        
                        locationName = "Fetching address..."
                        marker.title = "Loading..."
                        marker.showInfoWindow()

                        if (!view.overlays.contains(marker)) {
                            view.overlays.add(marker)
                        }
                        
                        // Fetch address immediately on tap
                        scope.launch {
                            val name = withContext(Dispatchers.IO) {
                                try {
                                    val geocoder = Geocoder(context, Locale.getDefault())
                                    val addresses = geocoder.getFromLocation(p.latitude, p.longitude, 1)
                                    if (!addresses.isNullOrEmpty()) {
                                        addresses[0].locality ?: addresses[0].subAdminArea ?: addresses[0].adminArea ?: addresses[0].getAddressLine(0) ?: "Unknown"
                                    } else "New Location"
                                } catch (e: Exception) {
                                    "Location (${p.latitude.toString().take(5)})"
                                }
                            }
                            locationName = name
                            marker.title = name
                            marker.showInfoWindow()
                        }

                        view.invalidate()
                        return true
                    }
                    override fun longPressHelper(p: GeoPoint): Boolean = false
                })
                view.overlays.removeAll { it is MapEventsOverlay }
                view.overlays.add(eventsOverlay)
            }
        )

        // Address Display Card
        locationName?.let { name ->
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (selectedLocation != null) {
            FloatingActionButton(
                onClick = {
                    if (uiState !is MapUiState.Saving) {
                        viewModel.saveLocation(selectedLocation!!, locationName ?: "New Location")
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(bottom = 60.dp, end = 16.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                if (uiState is MapUiState.Saving) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null)
                }
            }
        }
    }
}
