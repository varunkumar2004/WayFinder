package com.varunkumar.wayfinder.features.outdoor.domain

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import com.varunkumar.wayfinder.core.data.RouteInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

suspend fun getRouteDetails(
    apiKey: String,
    origin: LatLng,
    destination: LatLng
): RouteInfo? {
    val routingMode = "walking"
    val url = "https://maps.googleapis.com/maps/api/directions/json" +
            "?origin=${origin.latitude},${origin.longitude}" +
            "&destination=${destination.latitude},${destination.longitude}" +
            "&mode=$routingMode" +
            "&key=$apiKey"

    val client = OkHttpClient()
    val request = Request.Builder().url(url).build()

    return withContext(Dispatchers.IO) {
        try {
            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val json = JSONObject(response.body()?.string() ?: "")
                Log.d("RouteDetails", "API Response: $json")

                val routes = json.getJSONArray("routes")
                if (routes.length() > 0) {
                    val route = routes.getJSONObject(0)
                    val legs = route.getJSONArray("legs")

                    // Extract distance and duration from the first leg
                    val firstLeg = legs.getJSONObject(0)
                    val distanceText = firstLeg.getJSONObject("distance").getString("text")
                    val durationText = firstLeg.getJSONObject("duration").getString("text")

                    val path = mutableListOf<LatLng>()
                    for (i in 0 until legs.length()) {
                        val steps = legs.getJSONObject(i).getJSONArray("steps")
                        for (j in 0 until steps.length()) {
                            val step = steps.getJSONObject(j)
                            val polyline = step.getJSONObject("polyline").getString("points")
                            val points = PolyUtil.decode(polyline)
                            path.addAll(points)
                        }
                    }

                    // Return the new data class with all the information
                    RouteInfo(
                        polyline = path,
                        distance = distanceText,
                        duration = durationText
                    )
                } else {
                    Log.d("RouteDetails", "No routes found in API response.")
                    null
                }
            } else {
                val errorBody = response.body()?.string()
                Log.e("RouteDetails", "API request failed with code: ${response.code()}, body: $errorBody")
                null
            }
        } catch (e: Exception) {
            Log.e("RouteDetails", "Error during API request: ${e.message}", e)
            null
        }
    }
}
