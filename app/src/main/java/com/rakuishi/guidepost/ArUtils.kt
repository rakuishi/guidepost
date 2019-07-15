package com.rakuishi.guidepost

import com.google.ar.core.Camera
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
        latitude1: Double, longitude1: Double,
        latitude2: Double, longitude2: Double,
        orientation: Double
    ): AnchorNode? {
        val camera = arFragment.arSceneView.arFrame!!.camera
        if (camera.trackingState != TrackingState.TRACKING || renderable == null) {
            return null
        }

        val distance = LocationUtils.distance(latitude1, longitude1, latitude2, longitude2)
        val bearing = LocationUtils.bearing(latitude1, longitude1, latitude2, longitude2)
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