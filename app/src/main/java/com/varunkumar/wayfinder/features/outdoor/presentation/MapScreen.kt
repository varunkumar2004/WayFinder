package com.varunkumar.wayfinder.features.outdoor.presentation

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.varunkumar.wayfinder.R
import com.varunkumar.wayfinder.core.data.RouteInfo
import com.varunkumar.wayfinder.core.presentation.AppTopBar
import com.varunkumar.wayfinder.features.outdoor.data.BuildingByCategory
import java.util.Locale

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    mapsApi: String,
    context: Context,
    currentLocation: LatLng,
    cameraPositionState: CameraPositionState
) {
    val viewModel = hiltViewModel<MapViewModel>()
    val state by viewModel.fireStoreState.collectAsState()
    val showAlert by viewModel.showAlert.collectAsState()
    val selectedBuildingDestination by viewModel
        .selectedBuildingDestination.collectAsState()
    val routeInfo by viewModel.routeInfo.collectAsState()

    val mapStyleOptions = remember {
        MapStyleOptions(
            context.resources.openRawResource(R.raw.map_style)
                .bufferedReader().use { it.readText() }
        )
    }

    if (showAlert) {
        RouteAlert(
            modifier = Modifier.fillMaxWidth(),
            firestoreState = state,
            onDismissRequest = viewModel::hideAlertComposable,
            onCategorySelect = viewModel::updateSelectedBuildingCategory,
            // The signature of onDestinationSelect has been simplified
            // as the ViewModel now handles the API call internally.
            onDestinationSelect = { destination ->
                viewModel.updateSelectedBuildingDestination(destination)
            },
            currentLocation = currentLocation,
            mapsApi = mapsApi
        )
    }

    Scaffold(
        topBar = { AppTopBar() },
//        bottomBar = { AppBottomBar(modifier = Modifier.fillMaxWidth()) }
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(it)
        ) {
            CustomGoogleMap(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                mapsApi = mapsApi,
                mapStyleOptions = mapStyleOptions,
                currentLocation = currentLocation,
                cameraPositionState = cameraPositionState,
                selectedBuildingDestination = selectedBuildingDestination,
                routeInfo = routeInfo
            )

            AnimatedContent(
                modifier = Modifier,
                targetState = selectedBuildingDestination != null,
                transitionSpec = {
                    slideInVertically(
                        animationSpec = tween(durationMillis = 500),
                        initialOffsetY = { fullHeight -> fullHeight }
                    ) togetherWith slideOutVertically(
                        animationSpec = tween(durationMillis = 500),
                        targetOffsetY = { fullHeight -> fullHeight }
                    )
                },
                label = "Choose destination card animation"
            ) { routing ->
                BottomAnimatedContent(
                    modifier = Modifier
                        .padding(vertical = 5.dp)
                        .fillMaxWidth(),
                    routing = routing,
                    selectedBuildingDestination = selectedBuildingDestination,
                    routeInfo = routeInfo,
                    showRoutingAlert = viewModel::showAlertComposable,
                    onCurrentRoutingCancel = viewModel::onCurrentRoutingCancel
                )
            }
        }
    }
}

@Composable
private fun BottomAnimatedContent(
    modifier: Modifier = Modifier,
    routing: Boolean,
    selectedBuildingDestination: BuildingByCategory?,
    routeInfo: RouteInfo?,
    showRoutingAlert: () -> Unit,
    onCurrentRoutingCancel: () -> Unit
) {
    ListItem(
        modifier = modifier,
        headlineContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (routing) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        text = selectedBuildingDestination?.name?.capitalize(Locale.ROOT)
                            ?: "Unknown Location",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                } else {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        text = "Choose Destination",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        supportingContent = {
            AnimatedContent(
                targetState = routeInfo != null,
                transitionSpec = {
                    fadeIn(tween(300)) togetherWith fadeOut(tween(300))
                },
                label = "route info transition"
            ) { isRouteInfoReady ->
                if (isRouteInfoReady) {
                    Row(
                        modifier = Modifier,
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        routeInfo?.let {
                            Text(
                                text = it.distance,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.secondary,
                                fontStyle = FontStyle.Italic
                            )

                            Text(
                                text = it.duration,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.tertiary,
                                fontStyle = FontStyle.Italic
                            )
                        }
                    }
                } else {
                    if (routing) {
                        Text(
                            text = "calculating route...",
                            color = MaterialTheme.colorScheme.tertiary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        },
        trailingContent = {
            if (routing) {
                FilledIconButton(onClick = onCurrentRoutingCancel) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = null)
                }
            } else {
                FilledIconButton(onClick = showRoutingAlert) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "choose destination alert button"
                    )
                }
            }
        }
    )
}

@Composable
private fun CustomGoogleMap(
    modifier: Modifier = Modifier,
    mapsApi: String,
    currentLocation: LatLng,
    mapStyleOptions: MapStyleOptions,
    cameraPositionState: CameraPositionState,
    selectedBuildingDestination: BuildingByCategory?,
    routeInfo: RouteInfo? // Add the new RouteInfo object here
) {
    val southWest = LatLng(30.349528, 76.358667)
    val northEast = LatLng(30.359139, 76.373417)
    val bounds = LatLngBounds(southWest, northEast)

    LaunchedEffect(selectedBuildingDestination) {
        if (selectedBuildingDestination != null) {
            // Animate the camera to the destination location
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        selectedBuildingDestination.lat,
                        selectedBuildingDestination.long
                    ), 15f // A good zoom level for buildings
                ),
                durationMs = 1000
            )
        }
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            isBuildingEnabled = false,
            mapStyleOptions = mapStyleOptions,
            latLngBoundsForCameraTarget = bounds,
            isMyLocationEnabled = true,
            mapType = MapType.NORMAL
        ),
        uiSettings = MapUiSettings(
            mapToolbarEnabled = false,
            compassEnabled = false,
            myLocationButtonEnabled = false,
            tiltGesturesEnabled = false,
            rotationGesturesEnabled = true,
            zoomControlsEnabled = false // Customize map UI settings
        ),
        content = {
            selectedBuildingDestination?.let { it ->
                Marker(
                    state = MarkerState(position = LatLng(it.lat, it.long)),
                    title = "Destination Point"
                )
            }

            // Now we use the polyline from the RouteInfo object
            routeInfo?.polyline?.let {
                Polyline(
                    points = it,
                    color = MaterialTheme.colorScheme.tertiary,
                    width = 10f
                )
            }
        }
    )
}





//package com.varunkumar.wayfinder.features.outdoor.presentation
//
//import android.annotation.SuppressLint
//import android.content.Context
//import androidx.compose.animation.AnimatedContent
//import androidx.compose.animation.core.tween
//import androidx.compose.animation.fadeIn
//import androidx.compose.animation.fadeOut
//import androidx.compose.animation.slideInVertically
//import androidx.compose.animation.slideOutVertically
//import androidx.compose.animation.togetherWith
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Close
//import androidx.compose.material.icons.filled.Delete
//import androidx.compose.material.icons.filled.KeyboardArrowRight
//import androidx.compose.material3.FilledIconButton
//import androidx.compose.material3.Icon
//import androidx.compose.material3.ListItem
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.collectAsState
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.remember
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.text.font.FontStyle
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.hilt.navigation.compose.hiltViewModel
//import com.google.android.gms.maps.CameraUpdateFactory
//import com.google.android.gms.maps.model.LatLng
//import com.google.android.gms.maps.model.LatLngBounds
//import com.google.android.gms.maps.model.MapStyleOptions
//import com.google.maps.android.compose.CameraPositionState
//import com.google.maps.android.compose.GoogleMap
//import com.google.maps.android.compose.MapProperties
//import com.google.maps.android.compose.MapType
//import com.google.maps.android.compose.MapUiSettings
//import com.google.maps.android.compose.Marker
//import com.google.maps.android.compose.MarkerState
//import com.google.maps.android.compose.Polyline
//import com.varunkumar.wayfinder.R
//import com.varunkumar.wayfinder.core.data.RouteInfo
//import com.varunkumar.wayfinder.core.presentation.AppTopBar
//import com.varunkumar.wayfinder.features.outdoor.data.BuildingByCategory
//import java.util.Locale
//
//@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
//@Composable
//fun MapScreen(
//    modifier: Modifier = Modifier,
//    mapsApi: String,
//    context: Context,
//    currentLocation: LatLng,
//    cameraPositionState: CameraPositionState
//) {
//    val viewModel = hiltViewModel<MapViewModel>()
//    val state by viewModel.fireStoreState.collectAsState()
//    val showAlert by viewModel.showAlert.collectAsState()
//    val selectedBuildingDestination by viewModel
//        .selectedBuildingDestination.collectAsState()
//    val routeInfo by viewModel.routeInfo.collectAsState()
//
//    val mapStyleOptions = remember {
//        MapStyleOptions(
//            context.resources.openRawResource(R.raw.map_style)
//                .bufferedReader().use { it.readText() }
//        )
//    }
//
//    if (showAlert) {
//        RouteAlert(
//            modifier = Modifier.fillMaxWidth(),
//            firestoreState = state,
//            onDismissRequest = viewModel::hideAlertComposable,
//            onCategorySelect = viewModel::updateSelectedBuildingCategory,
//            // The signature of onDestinationSelect has been simplified
//            // as the ViewModel now handles the API call internally.
//            onDestinationSelect = viewModel::updateSelectedBuildingDestination,
//            currentLocation = currentLocation,
//            mapsApi = mapsApi
//        )
//    }
//
//    Scaffold(
//        topBar = { AppTopBar() },
////        bottomBar = { AppBottomBar(modifier = Modifier.fillMaxWidth()) }
//    ) {
//        Column(
//            modifier = modifier
//                .fillMaxWidth()
//                .padding(it)
//        ) {
//            CustomGoogleMap(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .weight(1f),
//                mapsApi = mapsApi,
//                mapStyleOptions = mapStyleOptions,
//                currentLocation = currentLocation,
//                cameraPositionState = cameraPositionState,
//                selectedBuildingDestination = selectedBuildingDestination,
//                routeInfo = routeInfo
//            )
//
//            AnimatedContent(
//                modifier = Modifier,
//                targetState = selectedBuildingDestination != null,
//                transitionSpec = {
//                    slideInVertically(
//                        animationSpec = tween(durationMillis = 500),
//                        initialOffsetY = { fullHeight -> fullHeight }
//                    ) togetherWith slideOutVertically(
//                        animationSpec = tween(durationMillis = 500),
//                        targetOffsetY = { fullHeight -> fullHeight }
//                    )
//                },
//                label = "Choose destination card animation"
//            ) { routing ->
//                BottomAnimatedContent(
//                    modifier = Modifier
//                        .padding(vertical = 5.dp)
//                        .fillMaxWidth(),
//                    routing = routing,
//                    selectedBuildingDestination = selectedBuildingDestination,
//                    routeInfo = routeInfo,
//                    showRoutingAlert = viewModel::showAlertComposable,
//                    onCurrentRoutingCancel = viewModel::onCurrentRoutingCancel
//                )
//            }
//        }
//    }
//}
//
//@Composable
//private fun BottomAnimatedContent(
//    modifier: Modifier = Modifier,
//    routing: Boolean,
//    selectedBuildingDestination: BuildingByCategory?,
//    routeInfo: RouteInfo?,
//    showRoutingAlert: () -> Unit,
//    onCurrentRoutingCancel: () -> Unit
//) {
//    ListItem(
//        modifier = modifier,
//        headlineContent = {
//            Row(
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.SpaceBetween
//            ) {
//                if (routing) {
//                    Text(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .weight(1f),
//                        text = selectedBuildingDestination?.name?.capitalize(Locale.ROOT)
//                            ?: "Unknown Location",
//                        style = MaterialTheme.typography.titleLarge,
//                        fontWeight = FontWeight.ExtraBold
//                    )
//                } else {
//                    Text(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .weight(1f),
//                        text = "Choose Destination",
//                        style = MaterialTheme.typography.titleLarge,
//                        fontWeight = FontWeight.Medium
//                    )
//                }
//            }
//        },
//        supportingContent = {
//            AnimatedContent(
//                targetState = routeInfo != null,
//                transitionSpec = {
//                    fadeIn(tween(300)) togetherWith fadeOut(tween(300))
//                },
//                label = "route info transition"
//            ) { isRouteInfoReady ->
//                if (isRouteInfoReady) {
//                    Row(
//                        modifier = Modifier,
//                        verticalAlignment = Alignment.CenterVertically,
//                        horizontalArrangement = Arrangement.spacedBy(10.dp)
//                    ) {
//                        routeInfo?.let {
//                            Text(
//                                text = it.distance,
//                                style = MaterialTheme.typography.bodyLarge,
//                                color = MaterialTheme.colorScheme.secondary,
//                                fontStyle = FontStyle.Italic
//                            )
//
//                            Text(
//                                text = it.duration,
//                                style = MaterialTheme.typography.bodyLarge,
//                                color = MaterialTheme.colorScheme.tertiary,
//                                fontStyle = FontStyle.Italic
//                            )
//                        }
//                    }
//                } else {
//                    if (routing) {
//                        Text(
//                            text = "calculating route...",
//                            color = MaterialTheme.colorScheme.tertiary,
//                            style = MaterialTheme.typography.bodySmall
//                        )
//                    }
//                }
//            }
//        },
//        trailingContent = {
//            if (routing) {
//                FilledIconButton(onClick = onCurrentRoutingCancel) {
//                    Icon(imageVector = Icons.Default.Close, contentDescription = null)
//                }
//            } else {
//                FilledIconButton(onClick = showRoutingAlert) {
//                    Icon(
//                        imageVector = Icons.Default.KeyboardArrowRight,
//                        contentDescription = "choose destination alert button"
//                    )
//                }
//            }
//        }
//    )
//}
//
//@Composable
//private fun CustomGoogleMap(
//    modifier: Modifier = Modifier,
//    mapsApi: String,
//    currentLocation: LatLng,
//    mapStyleOptions: MapStyleOptions,
//    cameraPositionState: CameraPositionState,
//    selectedBuildingDestination: BuildingByCategory?,
//    routeInfo: RouteInfo? // Add the new RouteInfo object here
//) {
//    val southWest = LatLng(30.349528, 76.358667)
//    val northEast = LatLng(30.359139, 76.373417)
//    val bounds = LatLngBounds(southWest, northEast)
//
//    LaunchedEffect(selectedBuildingDestination) {
//        if (selectedBuildingDestination != null) {
//            // Animate the camera to the destination location
//            cameraPositionState.animate(
//                update = CameraUpdateFactory.newLatLngZoom(
//                    LatLng(
//                        selectedBuildingDestination.lat,
//                        selectedBuildingDestination.long
//                    ), 15f // A good zoom level for buildings
//                ),
//                durationMs = 1000
//            )
//        }
//    }
//
//    GoogleMap(
//        modifier = modifier,
//        cameraPositionState = cameraPositionState,
//        properties = MapProperties(
//            isBuildingEnabled = false,
//            mapStyleOptions = mapStyleOptions,
//            latLngBoundsForCameraTarget = bounds,
//            isMyLocationEnabled = true,
//            mapType = MapType.NORMAL
//        ),
//        uiSettings = MapUiSettings(
//            mapToolbarEnabled = false,
//            compassEnabled = false,
//            myLocationButtonEnabled = false,
//            tiltGesturesEnabled = false,
//            rotationGesturesEnabled = true,
//            zoomControlsEnabled = false // Customize map UI settings
//        ),
//        content = {
//            selectedBuildingDestination?.let { it ->
//                Marker(
//                    state = MarkerState(position = LatLng(it.lat, it.long)),
//                    title = "Destination Point"
//                )
//            }
//
//            // Now we use the polyline from the RouteInfo object
//            routeInfo?.polyline?.let {
//                Polyline(
//                    points = it,
//                    color = MaterialTheme.colorScheme.tertiary,
//                    width = 10f
//                )
//            }
//        }
//    )
//}



/* 2nd iteration */

//package com.varunkumar.wayfinder.features.outdoor.presentation
//
//import android.annotation.SuppressLint
//import android.content.Context
//import androidx.compose.animation.AnimatedContent
//import androidx.compose.animation.core.tween
//import androidx.compose.animation.fadeIn
//import androidx.compose.animation.fadeOut
//import androidx.compose.animation.slideInVertically
//import androidx.compose.animation.slideOutVertically
//import androidx.compose.animation.togetherWith
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.heightIn
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Clear
//import androidx.compose.material.icons.filled.Delete
//import androidx.compose.material.icons.filled.KeyboardArrowRight
//import androidx.compose.material3.CardDefaults
//import androidx.compose.material3.ElevatedCard
//import androidx.compose.material3.FilledIconButton
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.InputChip
//import androidx.compose.material3.ListItem
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.OutlinedButton
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.SuggestionChip
//import androidx.compose.material3.Text
//import androidx.compose.material3.TextButton
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.collectAsState
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.text.font.FontStyle
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
//import androidx.hilt.navigation.compose.hiltViewModel
//import com.google.android.gms.maps.CameraUpdateFactory
//import com.google.android.gms.maps.model.LatLng
//import com.google.android.gms.maps.model.LatLngBounds
//import com.google.android.gms.maps.model.MapStyleOptions
//import com.google.maps.android.compose.CameraPositionState
//import com.google.maps.android.compose.GoogleMap
//import com.google.maps.android.compose.MapProperties
//import com.google.maps.android.compose.MapType
//import com.google.maps.android.compose.MapUiSettings
//import com.google.maps.android.compose.Marker
//import com.google.maps.android.compose.MarkerState
//import com.google.maps.android.compose.Polyline
//import com.varunkumar.wayfinder.R
//import com.varunkumar.wayfinder.core.data.RouteInfo
//import com.varunkumar.wayfinder.core.presentation.AppBottomBar
//import com.varunkumar.wayfinder.core.presentation.AppTopBar
//import com.varunkumar.wayfinder.features.outdoor.data.BuildingByCategory
//import java.util.Locale
//
//@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
//@Composable
//fun MapScreen(
//    modifier: Modifier = Modifier,
//    mapsApi: String,
//    context: Context,
//    currentLocation: LatLng,
//    cameraPositionState: CameraPositionState
//) {
//    val viewModel = hiltViewModel<MapViewModel>()
//    val state by viewModel.fireStoreState.collectAsState()
//    val showAlert by viewModel.showAlert.collectAsState()
//    val selectedBuildingDestination by viewModel
//        .selectedBuildingDestination.collectAsState()
//    val routeInfo by viewModel.routeInfo.collectAsState()
//
//    val mapStyleOptions = remember {
//        MapStyleOptions(
//            context.resources.openRawResource(R.raw.map_style)
//                .bufferedReader().use { it.readText() }
//        )
//    }
//
//    if (showAlert) {
//        RouteAlert(
//            modifier = Modifier.fillMaxWidth(),
//            firestoreState = state,
//            onDismissRequest = viewModel::hideAlertComposable,
//            onCategorySelect = viewModel::updateSelectedBuildingCategory,
//            onDestinationSelect = { destination, currentLocation, mapsApi ->
//                viewModel.updateSelectedBuildingDestination(destination, currentLocation, mapsApi)
//            },
//            currentLocation = currentLocation,
//            mapsApi = mapsApi
//        )
//    }
//
//    Scaffold(
//        topBar = { AppTopBar() },
////        bottomBar = { AppBottomBar(modifier = Modifier.fillMaxWidth()) }
//    ) {
//        Column(
//            modifier = modifier
//                .fillMaxWidth()
//                .padding(it)
//        ) {
//            CustomGoogleMap(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .weight(1f),
//                mapsApi = mapsApi,
//                mapStyleOptions = mapStyleOptions,
//                currentLocation = currentLocation,
//                cameraPositionState = cameraPositionState,
//                selectedBuildingDestination = selectedBuildingDestination,
//                routeInfo = routeInfo
//            )
//
//            AnimatedContent(
//                modifier = Modifier,
//                targetState = selectedBuildingDestination != null,
//                transitionSpec = {
//                    slideInVertically(
//                        animationSpec = tween(durationMillis = 500),
//                        initialOffsetY = { fullHeight -> fullHeight }
//                    ) togetherWith slideOutVertically(
//                        animationSpec = tween(durationMillis = 500),
//                        targetOffsetY = { fullHeight -> fullHeight }
//                    )
//                },
//                label = "Choose destination card animation"
//            ) { routing ->
//                BottomAnimatedContent(
//                    modifier = Modifier
//                        .padding(vertical = 5.dp)
//                        .fillMaxWidth(),
//                    routing = routing,
//                    selectedBuildingDestination = selectedBuildingDestination,
//                    routeInfo = routeInfo,
//                    showRoutingAlert = viewModel::showAlertComposable,
//                    onCurrentRoutingCancel = viewModel::onCurrentRoutingCancel
//                )
//            }
//        }
//    }
//}
//
//@Composable
//private fun BottomAnimatedContent(
//    modifier: Modifier = Modifier,
//    routing: Boolean,
//    selectedBuildingDestination: BuildingByCategory?,
//    routeInfo: RouteInfo?,
//    showRoutingAlert: () -> Unit,
//    onCurrentRoutingCancel: () -> Unit
//) {
//    ListItem(
//        modifier = modifier,
//        headlineContent = {
//            Row(
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.SpaceBetween
//            ) {
//                if (routing) {
//                    Text(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .weight(1f),
//                        text = selectedBuildingDestination?.name?.capitalize(Locale.ROOT)
//                            ?: "Unknown Location",
//                        style = MaterialTheme.typography.titleLarge,
//                        fontWeight = FontWeight.ExtraBold
//                    )
//                } else {
//                    Text(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .weight(1f),
//                        text = "Choose Destination",
//                        style = MaterialTheme.typography.titleLarge,
//                        fontWeight = FontWeight.Medium
//                    )
//                }
//            }
//        },
//        supportingContent = {
//            AnimatedContent(
//                targetState = routeInfo != null,
//                transitionSpec = {
//                    fadeIn(tween(300)) togetherWith fadeOut(tween(300))
//                },
//                label = "route info transition"
//            ) { isRouteInfoReady ->
//                if (isRouteInfoReady) {
//                    Row(
//                        modifier = Modifier,
//                        verticalAlignment = Alignment.CenterVertically,
//                        horizontalArrangement = Arrangement.spacedBy(10.dp)
//                    ) {
//                        routeInfo?.let {
//                            Text(
//                                text = it.distance,
//                                style = MaterialTheme.typography.bodyLarge,
//                                color = MaterialTheme.colorScheme.secondary,
//                                fontStyle = FontStyle.Italic
//                            )
//
//                            Text(
//                                text = it.duration,
//                                style = MaterialTheme.typography.bodyLarge,
//                                color = MaterialTheme.colorScheme.tertiary,
//                                fontStyle = FontStyle.Italic
//                            )
//                        }
//                    }
//                } else {
//                    if (routing) {
//                        Text(
//                            text = "calculating route...",
//                            color = MaterialTheme.colorScheme.tertiary,
//                            style = MaterialTheme.typography.bodySmall
//                        )
//                    }
//                }
//            }
//        },
//        trailingContent = {
//            if (routing) {
//                FilledIconButton(onClick = onCurrentRoutingCancel) {
//                    Icon(imageVector = Icons.Default.Clear, contentDescription = null)
//                }
//            } else {
//                FilledIconButton(onClick = showRoutingAlert) {
//                    Icon(
//                        imageVector = Icons.Default.KeyboardArrowRight,
//                        contentDescription = "choose destination alert button"
//                    )
//                }
//            }
//        }
//    )
//}
//
//@Composable
//private fun CustomGoogleMap(
//    modifier: Modifier = Modifier,
//    mapsApi: String,
//    currentLocation: LatLng,
//    mapStyleOptions: MapStyleOptions,
//    cameraPositionState: CameraPositionState,
//    selectedBuildingDestination: BuildingByCategory?,
//    routeInfo: RouteInfo? // Add the new RouteInfo object here
//) {
//    val southWest = LatLng(30.349528, 76.358667)
//    val northEast = LatLng(30.359139, 76.373417)
//    val bounds = LatLngBounds(southWest, northEast)
//
//    LaunchedEffect(selectedBuildingDestination) {
//        if (selectedBuildingDestination != null) {
//            // Animate the camera to the destination location
//            cameraPositionState.animate(
//                update = CameraUpdateFactory.newLatLngZoom(
//                    LatLng(
//                        selectedBuildingDestination.lat,
//                        selectedBuildingDestination.long
//                    ), 15f // A good zoom level for buildings
//                ),
//                durationMs = 1000
//            )
//        }
//    }
//
//    GoogleMap(
//        modifier = modifier,
//        cameraPositionState = cameraPositionState,
//        properties = MapProperties(
//            isBuildingEnabled = false,
//            mapStyleOptions = mapStyleOptions,
//            latLngBoundsForCameraTarget = bounds,
//            isMyLocationEnabled = true,
//            mapType = MapType.NORMAL
//        ),
//        uiSettings = MapUiSettings(
//            mapToolbarEnabled = false,
//            compassEnabled = false,
//            myLocationButtonEnabled = false,
//            tiltGesturesEnabled = false,
//            rotationGesturesEnabled = true,
//            zoomControlsEnabled = false // Customize map UI settings
//        ),
//        content = {
//            selectedBuildingDestination?.let { it ->
//                Marker(
//                    state = MarkerState(position = LatLng(it.lat, it.long)),
//                    title = "Destination Point"
//                )
//            }
//
//            // Now we use the polyline from the RouteInfo object
//            routeInfo?.polyline?.let {
//                Polyline(
//                    points = it,
//                    color = MaterialTheme.colorScheme.tertiary,
//                    width = 10f
//                )
//            }
//        }
//    )
//}




/* 1st iteration */

//package com.varunkumar.wayfinder.features.outdoor.presentation
//
//import android.annotation.SuppressLint
//import android.content.Context
//import androidx.compose.animation.AnimatedContent
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Clear
//import androidx.compose.material.icons.filled.Delete
//import androidx.compose.material.icons.filled.KeyboardArrowRight
//import androidx.compose.material3.CardDefaults
//import androidx.compose.material3.ElevatedCard
//import androidx.compose.material3.FilledIconButton
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.ListItem
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.OutlinedButton
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Text
//import androidx.compose.material3.TextButton
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.collectAsState
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.text.font.FontStyle
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
//import androidx.hilt.navigation.compose.hiltViewModel
//import com.google.android.gms.maps.model.LatLng
//import com.google.android.gms.maps.model.LatLngBounds
//import com.google.android.gms.maps.model.MapStyleOptions
//import com.google.maps.android.compose.CameraPositionState
//import com.google.maps.android.compose.GoogleMap
//import com.google.maps.android.compose.MapProperties
//import com.google.maps.android.compose.MapType
//import com.google.maps.android.compose.MapUiSettings
//import com.google.maps.android.compose.Marker
//import com.google.maps.android.compose.MarkerState
//import com.google.maps.android.compose.Polyline
//import com.varunkumar.wayfinder.R
//import com.varunkumar.wayfinder.core.data.RouteInfo
//import com.varunkumar.wayfinder.core.data.getRouteInfo
//import com.varunkumar.wayfinder.core.presentation.AppBottomBar
//import com.varunkumar.wayfinder.core.presentation.AppTopBar
//import com.varunkumar.wayfinder.features.outdoor.data.BuildingByCategory
//import com.varunkumar.wayfinder.features.outdoor.domain.getSmoothRoutePolyline
//import java.util.Locale
//
//@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
//@Composable
//fun MapScreen(
//    modifier: Modifier = Modifier,
//    mapsApi: String,
//    context: Context,
//    currentLocation: LatLng,
//    cameraPositionState: CameraPositionState
//) {
//    val viewModel = hiltViewModel<MapViewModel>()
//    val state by viewModel.fireStoreState.collectAsState()
//    val showAlert by viewModel.showAlert.collectAsState()
//    val selectedBuildingDestination by viewModel
//        .selectedBuildingDestination.collectAsState()
//
//    val mapStyleOptions = remember {
//        MapStyleOptions(
//            context.resources.openRawResource(R.raw.map_style)
//                .bufferedReader().use { it.readText() }
//        )
//    }
//
//    if (showAlert) {
//        RouteAlert(
//            modifier = Modifier.fillMaxWidth(),
//            firestoreState = state,
//            onDismissRequest = viewModel::hideAlertComposable,
//            onCategorySelect = viewModel::updateSelectedBuildingCategory,
//            onDestinationSelect = viewModel::updateSelectedBuildingDestination
//        )
//    }
//
//    Scaffold(
//        topBar = { AppTopBar() },
////        bottomBar = { AppBottomBar(modifier = Modifier.fillMaxWidth()) }
//    ) {
//        Column(
//            modifier = modifier
//                .fillMaxWidth()
//                .padding(it)
//        ) {
//            CustomGoogleMap(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .weight(1f),
//                mapsApi = mapsApi,
//                mapStyleOptions = mapStyleOptions,
//                currentLocation = currentLocation,
//                cameraPositionState = cameraPositionState,
//                selectedBuildingDestination = selectedBuildingDestination,
//            )
//
//            AnimatedContent(
//                modifier = Modifier,
//                targetState = selectedBuildingDestination != null,
//                label = "Choose destination card animation"
//            ) { routing ->
////                ElevatedCard(
////                    modifier = Modifier.padding(horizontal = 16.dp),
////                    onClick = viewModel::showAlertComposable,
////                    colors = CardDefaults.elevatedCardColors(
////                        containerColor =
////                        if (!routing) MaterialTheme.colorScheme.primary
////                        else MaterialTheme.colorScheme.surface,
////                        contentColor =
////                        if (!routing) MaterialTheme.colorScheme.surface
////                        else MaterialTheme.colorScheme.primary
////                    ),
////                    shape = RoundedCornerShape(10.dp)
////                ) {
//
//
////                    Row(
////                        modifier = Modifier
////                            .fillMaxWidth()
////                            .padding(10.dp),
////                        verticalAlignment = Alignment.CenterVertically,
////                        horizontalArrangement = Arrangement.Center
////                    ) {
////                        if (!routing) {
////                            Text(text = "Choose Destination")
////
////                            Spacer(modifier = Modifier.width(5.dp))
////
////                            Icon(
////                                imageVector = Icons.Default.KeyboardArrowRight,
////                                contentDescription = "choose destination alert button"
////                            )
////                        } else {
////                            Text(
////                                modifier = Modifier
////                                    .fillMaxWidth()
////                                    .weight(1f),
////                                text = selectedBuildingDestination?.name?.capitalize(Locale.ROOT)
////                                    ?: "Unknown Location",
////                                style = MaterialTheme.typography.titleLarge,
////                                fontStyle = FontStyle.Italic,
////                                fontWeight = FontWeight.Medium
////                            )
////
////                            Spacer(modifier = Modifier.width(5.dp))
////
////                            Icon(
////                                modifier = Modifier.clickable {
////                                    viewModel.onCurrentRoutingCancel()
////                                },
////                                imageVector = Icons.Default.Clear,
////                                contentDescription = "choose destination alert button"
////                            )
////                        }
////                    }
////                }
//                BottomAnimatedContent(
//                    modifier = Modifier
//                        .padding(vertical = 5.dp)
//                        .fillMaxWidth(),
//                    routing = routing,
//                    selectedBuildingDestination = selectedBuildingDestination,
//                    showRoutingAlert = viewModel::showAlertComposable,
//                    onCurrentRoutingCancel = viewModel::onCurrentRoutingCancel
//                )
//            }
//
//        }
//    }
//}
//
//@Composable
//private fun BottomAnimatedContent(
//    modifier: Modifier = Modifier,
//    routing: Boolean,
//    selectedBuildingDestination: BuildingByCategory?,
//    showRoutingAlert: () -> Unit,
//    onCurrentRoutingCancel: () -> Unit
//) {
//    ListItem(
//        modifier = modifier,
//        headlineContent = {
//            Row(
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.SpaceBetween
//            ) {
//                if (routing) {
//                    Text(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .weight(1f),
//                        text = selectedBuildingDestination?.name?.capitalize(Locale.ROOT)
//                            ?: "Unknown Location",
//                        style = MaterialTheme.typography.titleLarge,
//                        fontWeight = FontWeight.ExtraBold
//                    )
//                } else {
//                    Text(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .weight(1f),
//                        text = "Choose Destination",
//                        style = MaterialTheme.typography.titleLarge,
//                        fontWeight = FontWeight.Medium
//                    )
//                }
//            }
//        },
//        supportingContent = {
////            if (routing) {
//            Column(
//                modifier = Modifier
//                    .clip(RoundedCornerShape(15.dp))
//                    .background(MaterialTheme.colorScheme.tertiaryContainer)
//                    .padding(vertical = 5.dp, horizontal = 10.dp)
//            ) {
//                Text(text = "no choice for this")
//            }
////            }
//        },
//        trailingContent = {
//            if (routing) {
//                FilledIconButton(onClick = onCurrentRoutingCancel) {
//                    Icon(imageVector = Icons.Default.Delete, contentDescription = null)
//                }
//            } else {
//                FilledIconButton(onClick = showRoutingAlert) {
//                    Icon(
//                        imageVector = Icons.Default.KeyboardArrowRight,
//                        contentDescription = "choose destination alert button"
//                    )
//                }
//            }
//        }
//    )
//}
//
////@Composable
////private fun CustomGoogleMap(
////    modifier: Modifier = Modifier,
////    mapsApi: String,
////    currentLocation: LatLng,
////    mapStyleOptions: MapStyleOptions,
////    cameraPositionState: CameraPositionState,
////    selectedBuildingDestination: BuildingByCategory?
////) {
////    val southWest = LatLng(30.349528, 76.358667)
////    val northEast = LatLng(30.359139, 76.373417)
////    val bounds = LatLngBounds(southWest, northEast)
////
////    var routePolyline by remember {
////        mutableStateOf<List<LatLng>>(emptyList())
////    }
////
////    var routeInfo by remember{
////        mutableStateOf<RouteInfo?>(null)
////    }
////
////    LaunchedEffect(selectedBuildingDestination) {
////        if (selectedBuildingDestination != null) {
////            val result = getRouteInfo(
////                apiKey = mapsApi,
////                origin = currentLocation,
////                destination = LatLng(
////                    selectedBuildingDestination.lat,
////                    selectedBuildingDestination.long
////                )
////            )
////            routeInfo = result
////
////            val polyline = getSmoothRoutePolyline(
////                apiKey = mapsApi,
////                origin = currentLocation,
////                destination = LatLng(
////                    selectedBuildingDestination.lat,
////                    selectedBuildingDestination.long
////                )
////            )
////
////            routePolyline = polyline ?: emptyList()
////        } else routePolyline = emptyList()
////    }
////
////    GoogleMap(
////        modifier = modifier,
////        cameraPositionState = cameraPositionState,
////        properties = MapProperties(
////            isBuildingEnabled = false,
////            mapStyleOptions = mapStyleOptions,
////            latLngBoundsForCameraTarget = bounds,
////            isMyLocationEnabled = true,
////            mapType = MapType.NORMAL
////        ),
////        uiSettings = MapUiSettings(
////            mapToolbarEnabled = false,
////            compassEnabled = false,
////            myLocationButtonEnabled = false,
////            tiltGesturesEnabled = false,
////            rotationGesturesEnabled = true,
////            zoomControlsEnabled = false // Customize map UI settings
////        ),
////        content = {
////            selectedBuildingDestination?.let { it ->
////                Marker(
////                    state = MarkerState(position = LatLng(it.lat, it.long)),
////                    title = "Destination Point"
////                )
////            }
////
////            if (routePolyline.isNotEmpty()) {
////                Polyline(
////                    points = routePolyline,
////                    color = MaterialTheme.colorScheme.tertiary,
////                    width = 10f
////                )
////            }
////        }
////    )
////}
//
//@Composable
//private fun CustomGoogleMap(
//    modifier: Modifier = Modifier,
//    mapsApi: String,
//    currentLocation: LatLng,
//    mapStyleOptions: MapStyleOptions,
//    cameraPositionState: CameraPositionState,
//    selectedBuildingDestination: BuildingByCategory?,
//    routeInfo: RouteInfo? // Add the new RouteInfo object here
//) {
//    val southWest = LatLng(30.349528, 76.358667)
//    val northEast = LatLng(30.359139, 76.373417)
//    val bounds = LatLngBounds(southWest, northEast)
//
//    LaunchedEffect(selectedBuildingDestination) {
//        if (selectedBuildingDestination != null) {
//            // Note: This API call is now moved to the ViewModel for better architecture.
//            // The routeInfo parameter is now passed down from the ViewModel.
//            // This composable just observes the state.
//        }
//    }
//
//    GoogleMap(
//        modifier = modifier,
//        cameraPositionState = cameraPositionState,
//        properties = MapProperties(
//            isBuildingEnabled = false,
//            mapStyleOptions = mapStyleOptions,
//            latLngBoundsForCameraTarget = bounds,
//            isMyLocationEnabled = true,
//            mapType = MapType.NORMAL
//        ),
//        uiSettings = MapUiSettings(
//            mapToolbarEnabled = false,
//            compassEnabled = false,
//            myLocationButtonEnabled = false,
//            tiltGesturesEnabled = false,
//            rotationGesturesEnabled = true,
//            zoomControlsEnabled = false // Customize map UI settings
//        ),
//        content = {
//            selectedBuildingDestination?.let { it ->
//                Marker(
//                    state = MarkerState(position = LatLng(it.lat, it.long)),
//                    title = "Destination Point"
//                )
//            }
//
//            // Now we use the polyline from the RouteInfo object
//            routeInfo?.polyline?.let {
//                Polyline(
//                    points = it,
//                    color = MaterialTheme.colorScheme.tertiary,
//                    width = 10f
//                )
//            }
//        }
//    )
//}
//
