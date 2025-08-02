package com.varunkumar.wayfinder.core.domain

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.varunkumar.wayfinder.core.data.LocationState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LocationRepository(private val context: Context) {
    private val _locationState = MutableStateFlow<LocationState>(LocationState.Loading)

    val locationState = _locationState.asStateFlow()

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    init { requestCurrentLocation() }

    @SuppressLint("MissingPermission")
    fun requestCurrentLocation() {
        if (checkPermissionStatus()) {
            _locationState.update { LocationState.Error("Permission not granted") }
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                _locationState.value = LocationState.Success(it)
            } ?: run {
                _locationState.value = LocationState.Error("Location unavailable")
            }
        }
            .addOnFailureListener {
                _locationState.value = LocationState.Error(it.localizedMessage ?: "Unknown Error")
            }
    }

    private fun checkPermissionStatus(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    }
}