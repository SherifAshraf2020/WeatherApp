package com.example.weatherapp.presentation.map

import android.location.Geocoder
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
    var namesState by remember { mutableStateOf<Map<String, String?>?>(null) }
    val marker = remember { Marker(mapView) }

    val fetchingText = stringResource(id = R.string.fetching_address)
    val loadingText = stringResource(id = R.string.loading)
    val unknownText = stringResource(id = R.string.unknown)
    val newLocationText = stringResource(id = R.string.new_location)

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

                        locationName = fetchingText
                        marker.title = loadingText
                        marker.showInfoWindow()

                        if (!view.overlays.contains(marker)) {
                            view.overlays.add(marker)
                        }

                        scope.launch {
                            val results = withContext(Dispatchers.IO) {
                                try {
                                    val geocoderAr = Geocoder(context, Locale("ar"))
                                    val geocoderEn = Geocoder(context, Locale.ENGLISH)

                                    val addrAr = geocoderAr.getFromLocation(p.latitude, p.longitude, 1)?.firstOrNull()
                                    val addrEn = geocoderEn.getFromLocation(p.latitude, p.longitude, 1)?.firstOrNull()

                                    val nameAr = addrAr?.locality ?: addrAr?.subAdminArea ?: unknownText
                                    val nameEn = addrEn?.locality ?: addrEn?.subAdminArea ?: "Unknown"

                                    mapOf(
                                        "ar" to nameAr,
                                        "en" to nameEn,
                                        "countryAr" to addrAr?.countryName,
                                        "countryEn" to addrEn?.countryName
                                    )
                                } catch (e: Exception) {
                                    null
                                }
                            }

                            if (results != null) {
                                namesState = results
                                locationName = if (Locale.getDefault().language == "ar") results["ar"] else results["en"]
                                marker.title = locationName
                            } else {
                                locationName = newLocationText
                                marker.title = newLocationText
                            }
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
                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = name, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        if (selectedLocation != null) {
            FloatingActionButton(
                onClick = {
                    if (uiState !is MapUiState.Saving) {
                        viewModel.saveLocation(
                            lat = selectedLocation!!.latitude,
                            lon = selectedLocation!!.longitude,
                            nameAr = namesState?.get("ar") ?: unknownText,
                            nameEn = namesState?.get("en") ?: "Unknown",
                            countryAr = namesState?.get("countryAr"),
                            countryEn = namesState?.get("countryEn")
                        )
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