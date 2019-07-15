package com.rakuishi.guidepost

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.MaterialFactory
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ShapeFactory
import com.google.ar.sceneform.ux.ArFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), Scene.OnUpdateListener {

    private val REQUEST_PERMISSION: Int = 1000
    private lateinit var arFragment: ArFragment
    private var sphereRenderable: ModelRenderable? = null
    private var anchorNode: AnchorNode? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        arFragment = supportFragmentManager.findFragmentById(R.id.uxFragment) as ArFragment
        arFragment.arSceneView.planeRenderer.isEnabled = true
        arFragment.arSceneView.scene.addOnUpdateListener(this)

        val color = resources.getColor(R.color.colorPrimary, null)
        MaterialFactory.makeOpaqueWithColor(this, com.google.ar.sceneform.rendering.Color(color))
            .thenAccept { material ->
                sphereRenderable = ShapeFactory.makeSphere(0.025f, Vector3(0.0f, 0.0f, 0.0f), material)
            }

        // Start observing
        if (hasAccessFineLocationPermission()) {
            observe()
        } else {
            requestAccessFineLocationPermission()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun observe() {
        DevicePositionLiveData(this).observe(this, Observer { pair ->
            if (pair == null) return@Observer
            latLonTextView.text = "${pair.first?.latitude} / ${pair.first?.longitude}"
            orientationTextView.text = "${pair.second}"
            renderIfPossible(pair.first?.latitude, pair.first?.longitude, pair.second)
        })
    }

    @SuppressLint("SetTextI18n")
    private fun renderIfPossible(latitude: Double?, longitude: Double?, orientation: Double?) {
        if (anchorNode != null || latitude == null || longitude == null || orientation == null) return

        anchorNode =
            ArUtils.renderAnchorNode(
                arFragment,
                sphereRenderable,
                latitude,
                longitude,
                latitude,
                longitude,
                orientation
            )
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
