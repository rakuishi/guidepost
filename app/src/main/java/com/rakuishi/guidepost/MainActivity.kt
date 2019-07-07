package com.rakuishi.guidepost

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
        OrientationLiveData(this).observe(this, Observer { orientation ->
            if (orientation == null) return@Observer
            Toast.makeText(this, "$orientation", Toast.LENGTH_SHORT).show()
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
