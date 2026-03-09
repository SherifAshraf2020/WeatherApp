package com.example.weatherapp.data

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationTokenSource

class FusedLocationHelper(private val context: Context) {
    private val fusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context.applicationContext)

    @SuppressLint("MissingPermission")
    fun getFreshLocation(onLocationResult: (Location) -> Unit) {
        val cancellationTokenSource = CancellationTokenSource()

        // محاولة سريعة لجلب آخر موقع مسجل لكسر الشاشة البيضاء
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
            location?.let { onLocationResult(it) }
        }

        // طلب الموقع الحالي (مرة واحدة) بجودة عالية
        fusedLocationProviderClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource.token
        ).addOnSuccessListener { location ->
            location?.let { onLocationResult(it) }
        }.addOnFailureListener {
            cancellationTokenSource.cancel()
        }
    }

    fun isLocationEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
}