package com.varunkumar.wayfinder.core.data

data class RouteInfo(
    val polyline: List<com.google.android.gms.maps.model.LatLng>,
    val distance: String,
    val duration: String
)