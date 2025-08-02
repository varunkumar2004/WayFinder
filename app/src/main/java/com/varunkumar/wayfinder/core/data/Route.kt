package com.varunkumar.wayfinder.core.data

sealed class Route(
    val route: String
) {
    data object Home: Route(route = "home")
    data object Splash: Route(route = "Splash")
}