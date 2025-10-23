package com.varunkumar.wayfinder.core.domain

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * A repository for handling location services and providing location updates.
 */
class LocationRepository(private val context: Context) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    /**
     * Provides a cold Flow of real-time location updates.
     * The Flow will emit a new Location object whenever the device's location changes.
     */
    fun getLocationUpdates(): Flow<Location?> = callbackFlow {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 1000L
        ).build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                val lastLocation = locationResult.lastLocation
                if (lastLocation != null) {
                    trySend(lastLocation)
                }
            }
        }

        // Check for location permissions before requesting updates
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted, this Flow won't emit updates.
            // The calling component (like the ViewModel) should handle this.
            close(Exception("Location permissions not granted"))
            return@callbackFlow
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

        // The awaitClose block is crucial. It's called when the Flow is cancelled,
        // ensuring that we stop location updates to prevent battery drain.
        awaitClose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }
}