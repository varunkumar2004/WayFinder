package com.varunkumar.wayfinder.core.data

import android.location.Location

sealed class LocationState {
    data object Loading : LocationState()
    data class Success(val location: Location) : LocationState()
    data class Error(val message: String) : LocationState()
}
