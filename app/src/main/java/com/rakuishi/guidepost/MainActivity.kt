package com.rakuishi.guidepost

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.ux.ArFragment

class MainActivity : AppCompatActivity(), Scene.OnUpdateListener {

    private val REQUEST_PERMISSION: Int = 1000
    private lateinit var arFragment: ArFragment
    private lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        arFragment = supportFragmentManager.findFragmentById(R.id.ux_fragment) as ArFragment
        arFragment.arSceneView.planeRenderer.isEnabled = true
        arFragment.arSceneView.scene.addOnUpdateListener(this)

        textView = findViewById(R.id.text_view)

        // Start observing
        if (hasAccessFineLocationPermission()) {
            observe()
        } else {
            requestAccessFineLocationPermission()
        }
    }

    @SuppressLint("SetTextI18n")
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
            textView.text = "${pair.first?.latitude}\n${pair.first?.longitude}\n${pair.second}"
        })
    }

    // region AR

    override fun onUpdate(frameTime: FrameTime) {
        val camera = arFragment.arSceneView.arFrame!!.camera
        if (camera.trackingState == TrackingState.TRACKING) {
            // Hide instructions for how to scan for planes
            arFragment.planeDiscoveryController.hide()
        }
    }

    // endregion

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
