package com.example.runningrhino

import android.Manifest
import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult

class TrackingService : Service() {
    private val binder = LocalBinder()
    private var trackingCallback: TrackingCallback? = null
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    inner class LocalBinder : Binder() {
        fun getService(): TrackingService = this@TrackingService
    }

    override fun onBind(p0: Intent?): IBinder {
        Log.d("GPS", "onBind Service")
        return binder
    }

    fun setCallback(callback: TrackingCallback) {
       trackingCallback = callback
    }

    fun setLocationClient(client: FusedLocationProviderClient) {
        Log.d("GPS", "setLocationClient")
        fusedLocationProviderClient = client
    }

    fun startLocationUpdates() {
        Log.d("GPS", "startLocationUpdates")
        val locationRequest = LocationRequest.Builder(5000L).build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                for (location in p0.locations) {
                    Log.d("GPS", "${location.latitude}:${location.longitude}")
                    sendDataToActivity("${location.latitude}:${location.longitude}")
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    fun sendDataToActivity(data: String) {
        trackingCallback?.onDataReceived(data)
    }

    fun stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }
}