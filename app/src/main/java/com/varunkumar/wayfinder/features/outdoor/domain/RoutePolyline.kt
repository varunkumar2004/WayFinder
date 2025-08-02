package com.varunkumar.wayfinder.features.outdoor.domain

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

suspend fun getSmoothRoutePolyline(
    apiKey: String,
    origin: LatLng,
    destination: LatLng
): List<LatLng>? {
    val routingMode = "walking"
    val url = "https://maps.googleapis.com/maps/api/directions/json" +
            "?origin=${origin.latitude},${origin.longitude}" +
            "&destination=${destination.latitude},${destination.longitude}" +
            "&mode=$routingMode" +
            "&key=$apiKey"

    val client = OkHttpClient()
    val request = Request.Builder().url(url).build()

    return withContext(Dispatchers.IO) {
        val response = client.newCall(request).execute()
        if (response.isSuccessful) {
            val json = JSONObject(response.body()?.string() ?: "")

            Log.d("json body", json.toString())

            val routes = json.getJSONArray("routes")
            if (routes.length() > 0) {
                val route = routes.getJSONObject(0)
                val legs = route.getJSONArray("legs")
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
                path
            } else {
                null
            }
        } else {
            null
        }
    }
}