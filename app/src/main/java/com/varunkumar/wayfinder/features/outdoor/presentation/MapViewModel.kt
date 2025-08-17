package com.varunkumar.wayfinder.features.outdoor.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.varunkumar.wayfinder.core.data.RouteInfo
import com.varunkumar.wayfinder.core.domain.LocationRepository
import com.varunkumar.wayfinder.features.outdoor.data.BuildingByCategory
import com.varunkumar.wayfinder.features.outdoor.data.BuildingCategory
import com.varunkumar.wayfinder.features.outdoor.data.FirestoreState
import com.varunkumar.wayfinder.features.outdoor.domain.FirestoreRepository
import com.varunkumar.wayfinder.features.outdoor.domain.getRouteDetails
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val fireStoreRepository: FirestoreRepository,
    private val locationRepository: LocationRepository,
    private val apiKey: String
) : ViewModel() {
    var selectedBuildingDestination = MutableStateFlow<BuildingByCategory?>(null)
        private set

    var routeInfo = MutableStateFlow<RouteInfo?>(null)
        private set

    var showAlert = MutableStateFlow(false)
        private set

    private val _firestoreState = MutableStateFlow(FirestoreState())

    @OptIn(ExperimentalCoroutinesApi::class)
    val fireStoreState = _firestoreState.flatMapLatest { state ->
        state.selectedBuildingCategory?.let { loadBuildingByCategory(it) }
        _firestoreState
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FirestoreState())

    init {
        viewModelScope.launch { loadBuildingCategory() }
        observeRouteUpdates() // Start observing for live updates
    }

    private fun observeRouteUpdates() {
        viewModelScope.launch {
            combine(
                selectedBuildingDestination,
                locationRepository.getLocationUpdates()
            ) { destination, location ->
                Pair(destination, location)
            }.collect { (destination, currentLocation) ->
                if (destination != null && currentLocation != null) {
                    val result = getRouteDetails(
                        apiKey = apiKey,
                        origin = LatLng(currentLocation.latitude, currentLocation.longitude),
                        destination = LatLng(destination.lat, destination.long)
                    )
                    Log.d("live location result", result.toString())
                    routeInfo.update { result }
                }
            }
        }
    }

    fun showAlertComposable() {
        showAlert.update { true }
    }

    fun hideAlertComposable() {
        showAlert.update { false }
    }

    fun updateSelectedBuildingCategory(category: BuildingCategory) {
        _firestoreState.update { state ->
            state.copy(
                selectedBuildingCategory = category
            )
        }
    }

    fun updateSelectedBuildingDestination(destination: BuildingByCategory) {
        selectedBuildingDestination.update { destination }
    }

    fun onCurrentRoutingCancel() {
        selectedBuildingDestination.update { null }
        routeInfo.update { null }
    }

    private suspend fun loadBuildingCategory() {
        val buildings = fireStoreRepository.getBuildingCategory()

        _firestoreState.update {
            it.copy(
                buildingCategory = buildings,
                selectedBuildingCategory = null
            )
        }
    }

    private suspend fun loadBuildingByCategory(buildingType: BuildingCategory) {
        val markers = fireStoreRepository.getBuildingsByCategory(buildingType.name)
        _firestoreState.update { it.copy(buildingByCategory = markers) }
    }
}


/* 1st iteration */

//package com.varunkumar.wayfinder.features.outdoor.presentation
//
//import android.util.Log
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.google.android.gms.maps.model.LatLng
//import com.varunkumar.wayfinder.core.data.RouteInfo
//import com.varunkumar.wayfinder.features.outdoor.data.BuildingByCategory
//import com.varunkumar.wayfinder.features.outdoor.data.BuildingCategory
//import com.varunkumar.wayfinder.features.outdoor.data.FirestoreState
//import com.varunkumar.wayfinder.features.outdoor.domain.FirestoreRepository
//import com.varunkumar.wayfinder.features.outdoor.domain.getRouteDetails
//import dagger.hilt.android.lifecycle.HiltViewModel
//import kotlinx.coroutines.ExperimentalCoroutinesApi
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.SharingStarted
//import kotlinx.coroutines.flow.combine
//import kotlinx.coroutines.flow.flatMapLatest
//import kotlinx.coroutines.flow.stateIn
//import kotlinx.coroutines.flow.update
//import kotlinx.coroutines.launch
//import javax.inject.Inject
//
//@HiltViewModel
//class MapViewModel @Inject constructor(
//    private val fireStoreRepository: FirestoreRepository
//) : ViewModel() {
//    var selectedBuildingDestination = MutableStateFlow<BuildingByCategory?>(null)
//        private set
//
//    // New state to hold the RouteInfo object, which includes polyline, distance, and duration.
//    var routeInfo = MutableStateFlow<RouteInfo?>(null)
//        private set
//
//    var showAlert = MutableStateFlow(false)
//        private set
//
//    private val _firestoreState = MutableStateFlow(FirestoreState())
//
//    @OptIn(ExperimentalCoroutinesApi::class)
//    val fireStoreState = _firestoreState.flatMapLatest { state ->
//        state.selectedBuildingCategory?.let { loadBuildingByCategory(it) }
//        _firestoreState
//    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FirestoreState())
//
//    init {
//        viewModelScope.launch { loadBuildingCategory() }
//    }
//
//    fun showAlertComposable() {
//        showAlert.update { true }
//    }
//
//    fun hideAlertComposable() {
//        showAlert.update { false }
//    }
//
//    fun updateSelectedBuildingCategory(category: BuildingCategory) {
//        _firestoreState.update { state ->
//            state.copy(
//                selectedBuildingCategory = category
//            )
//        }
//    }
//
//    // Updated function to handle the new destination and fetch route details.
//    fun updateSelectedBuildingDestination(
//        destination: BuildingByCategory,
//        currentLocation: LatLng,
//        mapsApi: String
//    ) {
//        // Update the selected destination
//        selectedBuildingDestination.update { destination }
//
//        // Launch a coroutine to fetch the route details asynchronously
//        viewModelScope.launch {
//            val result = getRouteDetails(
//                apiKey = mapsApi,
//                origin = currentLocation,
//                destination = LatLng(destination.lat, destination.long)
//            )
//            // Update the routeInfo state with the result
//            routeInfo.update { result }
//        }
//    }
//
//    // Updated function to clear both destination and route information.
//    fun onCurrentRoutingCancel() {
//        selectedBuildingDestination.update { null }
//        routeInfo.update { null }
//    }
//
//    private suspend fun loadBuildingCategory() {
//        val buildings = fireStoreRepository.getBuildingCategory()
//
//        _firestoreState.update {
//            it.copy(
//                buildingCategory = buildings,
//                selectedBuildingCategory = null
//            )
//        }
//    }
//
//    private suspend fun loadBuildingByCategory(buildingType: BuildingCategory) {
//        val markers = fireStoreRepository.getBuildingsByCategory(buildingType.name)
//        _firestoreState.update { it.copy(buildingByCategory = markers) }
//    }
//}
