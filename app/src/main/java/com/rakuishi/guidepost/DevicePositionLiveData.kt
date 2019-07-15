package com.rakuishi.guidepost

import android.content.Context
import android.location.Location
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer

class DevicePositionLiveData(context: Context) : MediatorLiveData<Pair<Location?, Double?>>() {

    init {
        val locationLiveData = LocationLiveData(context)
        val orientationLiveData = OrientationLiveData(context)
        val devicePositionObserver = Observer<Any?> {
            this.value =
                Pair(locationLiveData.value, orientationLiveData.value)
        }
        this.addSource(locationLiveData, devicePositionObserver)
        this.addSource(orientationLiveData, devicePositionObserver)
    }
}