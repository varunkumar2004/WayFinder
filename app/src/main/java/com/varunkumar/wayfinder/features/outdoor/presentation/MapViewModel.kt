package com.varunkumar.wayfinder.features.outdoor.presentation

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.varunkumar.wayfinder.features.outdoor.data.BuildingByCategory
import com.varunkumar.wayfinder.features.outdoor.data.BuildingCategory
import com.varunkumar.wayfinder.features.outdoor.data.FirestoreState
import com.varunkumar.wayfinder.features.outdoor.domain.FirestoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val fireStoreRepository: FirestoreRepository
) : ViewModel() {
    var selectedBuildingDestination = MutableStateFlow<BuildingByCategory?>(null)
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