package com.rakuishi.guidepost

import android.location.Location
import android.util.Log
import android.widget.Toast
import com.google.ar.core.Pose
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import kotlin.math.cos
import kotlin.math.sin

object ArUtils {

    fun renderAnchorNode(
        arFragment: ArFragment, renderable: ModelRenderable?,
        location1: Location, location2: Location,
        orientation: Double
    ): AnchorNode? {
        val camera = arFragment.arSceneView.arFrame!!.camera
        if (camera.trackingState != TrackingState.TRACKING || renderable == null) {
            return null
        }

        val distance =
            LocationUtils.distance(location1.latitude, location1.longitude, location2.latitude, location2.longitude)
        val bearing =
            LocationUtils.bearing(location1.latitude, location1.longitude, location2.latitude, location2.longitude)
        val rotation = bearing - orientation
        val radRotation = Math.toRadians(rotation)
        val zRotated = (-distance * cos(radRotation)).toFloat()
        val xRotated = (distance * sin(radRotation)).toFloat()
        val y = camera.displayOrientedPose.ty()
        val translation = Pose.makeTranslation(xRotated, y, zRotated)
        val pose = camera.displayOrientedPose.compose(translation).extractTranslation()
        val anchor = arFragment.arSceneView.session!!.createAnchor(pose)
        val anchorNode = AnchorNode(anchor)
        anchorNode.renderable = renderable
        anchorNode.setParent(arFragment.arSceneView.scene)

        return anchorNode
    }
}