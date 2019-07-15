package com.rakuishi.guidepost

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
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
import com.google.ar.sceneform.rendering.ShapeFactory
import com.google.ar.sceneform.ux.ArFragment
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.CompletableFuture


class MainActivity : AppCompatActivity(), Scene.OnUpdateListener {

    private val REQUEST_PERMISSION: Int = 1000
    private val MIN_DISTANCE: Int = 1

    private lateinit var arFragment: ArFragment
    private var anchorNodes: ArrayList<AnchorNode> = arrayListOf()
    private var locations: ArrayList<Location> = arrayListOf()
    private var orientation: Double? = null
    private var isRecording: Boolean = true
    private var isObserving: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        arFragment = supportFragmentManager.findFragmentById(R.id.uxFragment) as ArFragment
        arFragment.arSceneView.planeRenderer.isEnabled = true
        arFragment.arSceneView.scene.addOnUpdateListener(this)

        button.setOnClickListener {
            if (isRecording) {
                isRecording = false
                renderAnchorNodes()
            } else {
                locations = arrayListOf()
                orientation = null
                anchorNodes.forEach { anchorNode -> anchorNode.setParent(null) }
                anchorNodes = arrayListOf()
                isRecording = true
                button.isEnabled = false
                button.text = getString(R.string.render, locations.size)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun observe() {
        DevicePositionLiveData(this).observe(this, Observer { pair ->
            if (pair == null) return@Observer
            latLonTextView.text = "${pair.first?.latitude} / ${pair.first?.longitude}"
            orientationTextView.text = "${pair.second}"

            if (isRecording) {
                if (pair.first != null) {
                    if (locations.size == 0 || LocationUtils.distance(locations.last(), pair.first!!) > MIN_DISTANCE) {
                        locations.add(pair.first!!)
                    }
                }

                if (pair.second != null) {
                    orientation = pair.second
                }

                button.isEnabled = locations.size > 0 && orientation != null
                button.text = getString(R.string.render, locations.size)
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun renderAnchorNodes() {
        if (locations.size == 0 || orientation == null) return

        val camera = arFragment.arSceneView.arFrame!!.camera
        if (camera.trackingState != TrackingState.TRACKING) return

        val color = resources.getColor(R.color.colorPrimary, null)
        val futures: ArrayList<CompletableFuture<AnchorNode>> = arrayListOf()

        locations.forEach { location ->
            val future: CompletableFuture<AnchorNode> =
                MaterialFactory.makeOpaqueWithColor(this, com.google.ar.sceneform.rendering.Color(color))
                    .thenApply { material ->
                        val anchorNode = ArUtils.renderAnchorNode(
                            arFragment, ShapeFactory.makeSphere(0.025f, Vector3(0.0f, 0.0f, 0.0f), material),
                            location, locations.last(), orientation!!
                        )
                        if (anchorNode != null) anchorNodes.add(anchorNode)
                        anchorNode
                    }
            futures.add(future)
        }

        CompletableFuture.allOf(*futures.toTypedArray()).whenComplete { _, u ->
            if (u == null) {
                button.text = getString(R.string.rendered, anchorNodes.size)
            }
        }
    }

    // region AR

    override fun onUpdate(frameTime: FrameTime) {
        val camera = arFragment.arSceneView.arFrame!!.camera
        if (camera.trackingState == TrackingState.TRACKING) {
            // Hide instructions for how to scan for planes
            arFragment.planeDiscoveryController.hide()

            if (!isObserving) {
                isObserving = true

                if (hasAccessFineLocationPermission()) {
                    observe()
                } else {
                    requestAccessFineLocationPermission()
                }
            }
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            observe()
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    // endregion
}
