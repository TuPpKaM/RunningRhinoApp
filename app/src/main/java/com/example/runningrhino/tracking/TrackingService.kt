package com.example.runningrhino.tracking

import android.Manifest
import android.app.Notification
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Binder
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.example.runningrhino.Constants
import com.example.runningrhino.R
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
        startForeground(Constants.FOREGROUND_SERVICE_NOTIFICATION_ID, createServiceNotification())
        return binder
    }

    fun setCallback(callback: TrackingCallback) {
        trackingCallback = callback
    }

    fun setLocationClient(client: FusedLocationProviderClient) {
        Log.d("GPS", "setLocationClient")
        fusedLocationProviderClient = client
    }

    private fun createServiceNotification(): Notification {
        val notificationLayout =
            RemoteViews(packageName, R.layout.notification_foreground_gps_small)
        val notificationLayoutExpanded =
            RemoteViews(packageName, R.layout.notification_foreground_gps_large)

        val builder = NotificationCompat.Builder(this, Constants.FOREGROUND_SERVICE_CHANNEL)
            .setSmallIcon(R.drawable.rhino)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(notificationLayout)
            .setCustomBigContentView(notificationLayoutExpanded)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        return builder.build()
    }

    fun startLocationUpdates() {
        Log.d("GPS", "startLocationUpdates")
        val locationRequest = LocationRequest.Builder(Constants.LOCATION_UPDATE_FREQ).build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                for (location in p0.locations) {
                    Log.d(
                        "GPS",
                        "${location.latitude}:${location.longitude}:${location.speed}:${location.time}"
                    )
                    sendDataToActivity(location)
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

    fun sendDataToActivity(data: Location) {
        trackingCallback?.onDataReceived(data)
    }

    fun stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }
}