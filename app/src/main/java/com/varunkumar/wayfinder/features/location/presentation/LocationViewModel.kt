package com.varunkumar.wayfinder.features.location.presentation

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.varunkumar.wayfinder.features.location.data.HomeLocationState
import com.varunkumar.wayfinder.core.domain.LocationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * ViewModel for handling location-related data and logic.
 */
@HiltViewModel
class LocationViewModel @Inject constructor(
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HomeLocationState())
    val state: StateFlow<HomeLocationState> = _state.asStateFlow()

    // Flag to request location permissions and start updates once.
    private var isRequestingLocation = false

    /**
     * Starts listening for location updates from the repository.
     */
    fun startLocationUpdates() {
        if (isRequestingLocation) return
        isRequestingLocation = true

        locationRepository.getLocationUpdates()
            .onEach { location: Location? ->
                // Update the state with the new location.
                if (location != null) {
                    _state.update { it.copy(currentLocation = location) }
                }
            }
            .launchIn(viewModelScope)
    }

}