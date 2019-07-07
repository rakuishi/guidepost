package com.rakuishi.guidepost

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer

class MainActivity : AppCompatActivity() {

    private val REQUEST_PERMISSION: Int = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (hasAccessFineLocationPermission()) {
            observe()
        } else {
            requestAccessFineLocationPermission()
        }
    }

    private fun observe() {
        val locationLiveData = LocationLiveData(this)
        val orientationLiveData = OrientationLiveData(this)
        val devicePositionLiveData = MediatorLiveData<Pair<Location?, Double?>>()
        val devicePositionObserver = Observer<Any?> {
            devicePositionLiveData.value =
                Pair(locationLiveData.value, orientationLiveData.value)
        }
        devicePositionLiveData.addSource(locationLiveData, devicePositionObserver)
        devicePositionLiveData.addSource(orientationLiveData, devicePositionObserver)
        devicePositionLiveData.observe(this, Observer { pair ->
            if (pair == null) return@Observer
            val message = "${pair.first?.latitude}, ${pair.first?.longitude}, ${pair.second}"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        })
    }

    // region Permission

    private fun hasAccessFineLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    private fun requestAccessFineLocationPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_PERMISSION
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            observe()
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    // endregion
}
