package com.varunkumar.wayfinder.core.presentation

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.varunkumar.wayfinder.R
import com.varunkumar.wayfinder.core.data.Route
import com.varunkumar.wayfinder.features.location.data.HomeLocationState
import com.varunkumar.wayfinder.features.location.presentation.LocationViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,
    state: HomeLocationState,
    locationViewModel: LocationViewModel, // Accept ViewModel here
    navHostController: NavHostController
) {
    val permissionState =
        rememberPermissionState(permission = android.Manifest.permission.ACCESS_FINE_LOCATION)

    LaunchedEffect(permissionState.status.isGranted) {
        if (permissionState.status.isGranted) {
            // Start location updates when permission is granted
            locationViewModel.startLocationUpdates()
        }
    }

    LaunchedEffect(state.currentLocation) {
        // Navigate to the next screen only when a location has been received
        if (state.currentLocation != null) {
            delay(500)
            navHostController.navigate(Route.Home.route)
        }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape),
            painter = painterResource(id = R.drawable.icon_png),
            contentDescription = "Android icon"
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            text = "TU WayFinder"
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when {
                permissionState.status.isGranted -> {
                    // Show a progress indicator while waiting for the first location
                    if (state.currentLocation == null) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(30.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            trackColor = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }

                permissionState.status.shouldShowRationale -> {
                    Text("Location permission is needed to continue.")

                    Button(onClick = { permissionState.launchPermissionRequest() }) {
                        Text("Grant Permission")
                    }
                }

                else -> {
                    Button(onClick = { permissionState.launchPermissionRequest() }) {
                        Text("Request Permission")
                    }
                }
            }
        }
    }
}



/* 1st iteration */

//package com.varunkumar.wayfinder.core.presentation
//
//import android.window.SplashScreen
//import androidx.compose.animation.core.Animatable
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.Button
//import androidx.compose.material3.CircularProgressIndicator
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.remember
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
//import androidx.navigation.NavHostController
//import com.google.accompanist.permissions.ExperimentalPermissionsApi
//import com.google.accompanist.permissions.isGranted
//import com.google.accompanist.permissions.rememberPermissionState
//import com.google.accompanist.permissions.shouldShowRationale
//import com.varunkumar.wayfinder.R
//import com.varunkumar.wayfinder.core.data.Route
//import com.varunkumar.wayfinder.features.location.data.HomeLocationState
//import kotlinx.coroutines.delay
//
//@OptIn(ExperimentalPermissionsApi::class)
//@Composable
//fun SplashScreen(
//    modifier: Modifier = Modifier,
//    state: HomeLocationState,
//    updateLocationPermission: () -> Unit,
//    navHostController: NavHostController
//) {
//    val scale = remember { Animatable(0f) }
//
//    val permissionState =
//        rememberPermissionState(permission = android.Manifest.permission.ACCESS_FINE_LOCATION)
//
//    LaunchedEffect(permissionState.status.isGranted) {
//        if (permissionState.status.isGranted) {
//            updateLocationPermission()
//        }
//    }
//
//    Column(
//        modifier = modifier,
//        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Image(
//            modifier = Modifier
//                .size(100.dp)
//                .clip(CircleShape),
//            painter = painterResource(id = R.drawable.icon_png),
//            contentDescription = "Android icon"
//        )
//
//        Spacer(modifier = Modifier.height(10.dp))
//
//        Text(
//            modifier = Modifier.fillMaxWidth(),
//            textAlign = TextAlign.Center,
//            style = MaterialTheme.typography.titleLarge,
//            color = MaterialTheme.colorScheme.primary,
//            text = "TU WayFinder"
//        )
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        Column(
//            modifier = Modifier.fillMaxWidth(),
//            verticalArrangement = Arrangement.spacedBy(16.dp),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            when {
//                permissionState.status.isGranted -> {
//                    if (state.currentLocation != null) {
//                        LaunchedEffect(Unit) {
//                            delay(500)
//                            navHostController.navigate(Route.Home.route)
//                        }
//                    } else {
//                        CircularProgressIndicator(
//                            modifier = Modifier.size(30.dp),
//                            color = MaterialTheme.colorScheme.tertiaryContainer,
//                            trackColor = MaterialTheme.colorScheme.tertiary
//                        )
//                    }
//                }
//
//                permissionState.status.shouldShowRationale -> {
//                    Text("Location permission is needed to continue.")
//
//                    Button(onClick = { permissionState.launchPermissionRequest() }) {
//                        Text("Grant Permission")
//                    }
//                }
//
//                else -> {
//                    Button(onClick = { permissionState.launchPermissionRequest() }) {
//                        Text("Request Permission")
//                    }
//                }
//            }
//        }
//    }
//}