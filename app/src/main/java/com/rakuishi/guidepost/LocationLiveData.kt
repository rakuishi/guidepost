package com.rakuishi.guidepost

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.SystemClock
import androidx.lifecycle.LiveData
import com.google.android.gms.location.*


class LocationLiveData(context: Context) : LiveData<Location>() {

    private val fusedLocationProviderClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    private val callback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult?) {
            val location = filteredLocation(result)
            if (location != null)
                value = location
        }
    }

    @SuppressLint("MissingPermission")
    override fun onActive() {
        super.onActive()
        val locationRequest = LocationRequest().apply {
            interval = 5000
            fastestInterval = 2500
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, callback, null)
    }

    override fun onInactive() {
        super.onInactive()
        fusedLocationProviderClient.removeLocationUpdates(callback)
    }

    // https://link.medium.com/5me7uTDVkY
    private fun filteredLocation(result: LocationResult?): Location? {
        val location = result?.lastLocation ?: return null

        // discard the location 10secs ago
        val age = (SystemClock.elapsedRealtimeNanos() - location.elapsedRealtimeNanos) / 1000000
        if (age > 10 * 1000) {
            return null
        }

        // adopt location within 0~20m
        if (0 > location.accuracy || location.accuracy > 20) {
            return null
        }

        return location
    }
}