package com.example.runningrhino

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.runningrhino.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, TrackingCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var mService: TrackingService
    private var mBound: Boolean = false

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            Log.d("GPS", "onServiceConnected")
            val binder = service as TrackingService.LocalBinder
            mService = binder.getService()
            mBound = true
            if (checkLocationPermissions()) {
                startLocationTracking()
            }
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            Log.d("GPS", "onServiceDisconnected")
            mBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("GPS", "onCreate maps")
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }
    override fun onMapReady(googleMap: GoogleMap) {
        Log.d("GPS", "onMapReady")
        mMap = googleMap

        val start = LatLng(55.60932012493261, 12.999741809051638)
        mMap.addMarker(MarkerOptions().position(start).title("Start"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(start, 16F))

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        Intent(this, TrackingService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }

    }

    private fun checkLocationPermissions(): Boolean {
        Log.d("GPS", "checkLocationPermissions")
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                101
            )
            return false
        }
        return true
    }

    private fun startLocationTracking(){
        Log.d("GPS", "startLocationTracking")
        mService.setCallback(this@MapsActivity)
        mService.setLocationClient(fusedLocationProviderClient)
        mService.startLocationUpdates()

    }

    override fun onDataReceived(data: String) {
        Log.d("GPS", "-----$data")
    }
}