package com.varunkumar.wayfinder.features.location.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.varunkumar.wayfinder.core.data.LocationState
import com.varunkumar.wayfinder.core.domain.LocationRepository
import com.varunkumar.wayfinder.features.location.data.HomeLocationState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class LocationViewModel @Inject constructor(
    private val locationRepository: LocationRepository
) : ViewModel() {
    private val _homeLocationState = MutableStateFlow(HomeLocationState())

    val state = combine(
        locationRepository.locationState,
        _homeLocationState
    ) { locationState, homeScreenState ->
        when (locationState) {
            is LocationState.Success -> {
                _homeLocationState.update { it.copy(currentLocation = locationState.location) }
            }

            else -> {}
        }

        Log.d("locationState", locationState.toString())
        homeScreenState
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeLocationState())

    fun requestLocation() {
        locationRepository.requestCurrentLocation()
    }
}