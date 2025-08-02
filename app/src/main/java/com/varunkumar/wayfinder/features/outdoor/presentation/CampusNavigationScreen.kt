package com.varunkumar.wayfinder.features.outdoor.presentation

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.location.LocationServices
import com.google.maps.android.compose.GoogleMap
import com.google.type.LatLng

@SuppressLint("MissingPermission")
@Composable
fun CampusNavigationScreen(
    apiKey: String,
    destLatLng: LatLng
) {
    val context = LocalContext.current
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var polylinePoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }

    // Get user location
//    LaunchedEffect(Unit) {
//        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
//            location?.let {
//                val origin = LatLng(it.latitude, it.longitude)
//                userLocation = origin
//                // Call Directions API
//                fetchRoute(apiKey, origin, destLatLng) { points ->
//                    polylinePoints = points
//                }
//            }
//        }
//    }
//
}
