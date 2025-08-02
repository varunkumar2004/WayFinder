package com.varunkumar.wayfinder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.rememberCameraPositionState
import com.varunkumar.wayfinder.core.data.Route
import com.varunkumar.wayfinder.core.presentation.SplashScreen
import com.varunkumar.wayfinder.features.location.presentation.LocationViewModel
import com.varunkumar.wayfinder.features.outdoor.data.BuildingByCategory
import com.varunkumar.wayfinder.features.outdoor.presentation.MapScreen
import com.varunkumar.wayfinder.features.outdoor.presentation.MapViewModel
import com.varunkumar.wayfinder.ui.theme.WayfinderTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        MapsInitializer.initialize(applicationContext)

        setContent {
            WayfinderTheme {
                val route by remember {
                    mutableStateOf<Route>(Route.Splash)
                }

                val screenModifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)

                val navController = rememberNavController()

                val locationViewModel = hiltViewModel<LocationViewModel>()
                val homeLocationState by locationViewModel.state.collectAsState()

                val currentLocation by remember(homeLocationState.currentLocation) {
                    derivedStateOf {
                        LatLng(
                            homeLocationState.currentLocation?.latitude ?: 0.0,
                            homeLocationState.currentLocation?.longitude ?: 0.0
                        )
                    }
                }

                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(
                        currentLocation, 5f
                    )
                }

                NavHost(
                    navController = navController,
                    startDestination = route.route
                ) {
                    composable(route = Route.Splash.route) {
                        SplashScreen(
                            modifier = screenModifier,
                            navHostController = navController,
                            state = homeLocationState,
                            updateLocationPermission = {
                                lifecycleScope.launch {
                                    locationViewModel.requestLocation()

                                    cameraPositionState.animate(
                                        update = CameraUpdateFactory.newLatLngZoom(
                                            currentLocation, 5f
                                        ),
                                        durationMs = 1000
                                    )
                                }
                            }
                        )
                    }

                    composable(
                        route = Route.Home.route
                    ) {
                        val mapsApi = stringResource(id = R.string.google_maps_key)

                        MapScreen(
                            modifier = screenModifier,
                            context = this@MainActivity,
                            mapsApi = mapsApi,
                            currentLocation = currentLocation,
                            cameraPositionState = cameraPositionState
                        )
                    }
                }
            }
        }
    }
}
