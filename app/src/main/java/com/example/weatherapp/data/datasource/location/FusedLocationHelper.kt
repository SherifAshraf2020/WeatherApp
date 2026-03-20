package com.example.weatherapp.data.datasource.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

class FusedLocationHelper(private val context: Context) {
    private val fusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context.applicationContext)

    @SuppressLint("MissingPermission")
    fun getFreshLocation(onResult: (Location?) -> Unit) {
        if (!isLocationEnabled()) {
            onResult(null)
            return
        }

        val cancellationTokenSource = CancellationTokenSource()

        fusedLocationProviderClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource.token
        ).addOnCompleteListener { task ->
            if (task.isSuccessful && task.result != null) {
                onResult(task.result)
            } else {
                fusedLocationProviderClient.lastLocation.addOnCompleteListener { lastTask ->
                    onResult(if (lastTask.isSuccessful) lastTask.result else null)
                }
            }
        }
    }

    fun isLocationEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
}