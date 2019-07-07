package com.rakuishi.guidepost

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import androidx.lifecycle.LiveData
import com.google.android.gms.location.*

class LocationLiveData(context: Context) : LiveData<Location>() {

    private val fusedLocationProviderClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    private val callback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult?) {
            val location = result?.lastLocation ?: return
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
}