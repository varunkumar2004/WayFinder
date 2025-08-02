package com.varunkumar.wayfinder.features.outdoor.data

data class FirestoreState(
    val buildingCategory: List<BuildingCategory> = emptyList(),
    val buildingByCategory: List<BuildingByCategory> = emptyList(),
    val selectedBuildingCategory: BuildingCategory? = null
)