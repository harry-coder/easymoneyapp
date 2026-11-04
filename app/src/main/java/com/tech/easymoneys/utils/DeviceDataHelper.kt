package com.tech.easymoneys.utils

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

object DeviceDataHelper {

    @SuppressLint("MissingPermission")
    fun getLastKnownLocation(context: Context, callback: (String) -> Unit) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    callback("Lat: ${location.latitude}, Lon: ${location.longitude}")
                } else {
                    callback("Location not available")
                }
            }
            .addOnFailureListener {
                callback("Error: ${it.message}")
            }
    }
}
