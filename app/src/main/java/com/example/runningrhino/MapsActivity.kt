package com.example.runningrhino

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.graphics.Color
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
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, TrackingCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var mService: TrackingService
    private var mBound: Boolean = false

    private var locationHistory: ArrayList<LatLng>? = null
    private var previousLocation: LatLng? = null
    private var polylines: ArrayList<Polyline>? = null
    private val debug = true
    private var testint: Int = 0

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            Log.d("GPS", "onServiceConnected")
            val binder = service as TrackingService.LocalBinder
            mService = binder.getService()
            mBound = true
            if (checkLocationPermissions()) {
                startLocationTracking()
            } //TODO: retry after asking for permission
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

        val start = LatLng(56.0705867, 12.7036555)
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
            ActivityCompat.requestPermissions( //TODO: move to own function
                this,
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                101
            )
            return false
        }
        return true
    }

    private fun startLocationTracking() {
        Log.d("GPS", "startLocationTracking")
        mService.setCallback(this@MapsActivity)
        mService.setLocationClient(fusedLocationProviderClient)
        mService.startLocationUpdates()

    }

    /**
     * Changes format from string to LatLng
     * @param data String containing the location "lat:lng"
     * @return LatLng
     */
    private fun makeLatLng(data: String): LatLng {
        val cordString = data.split(":")
        var lat = cordString[0].toDouble()
        var lng = cordString[1].toDouble()

        //make location drift during debug
        if (debug) {
            lat += (testint * 0.0001)
            lng -= (testint * 0.0001)
            testint += 1
        }

        return LatLng(lat, lng)
    }

    /**
     * Draws a continuous polyline on the Map with each update
     * @param currentLocation Draws a polyline to this location
     */
    private fun drawPolyline(currentLocation: LatLng) {
        val lineOptions = PolylineOptions()
            .add(LatLng(previousLocation!!.latitude, previousLocation!!.longitude))
            .add(LatLng(currentLocation.latitude, currentLocation.longitude))
            .color(Color.RED)
            .width(5f)

        val polyline: Polyline = mMap.addPolyline(lineOptions)
        if (polylines == null) {
            polylines = arrayListOf(polyline)
        } else {
            polylines!!.add(polyline)
        }

        previousLocation = currentLocation
    }

    /**
     * Callback from TrackingService. Saves the new cordinates in the history and
     * calls drawPolyline to add it on the Map
     * @param data String containing the location "lat:lng"
     */
    override fun onDataReceived(data: String) {
        Log.d("GPS", "-----$data")
        if (locationHistory == null) {
            locationHistory = arrayListOf(makeLatLng(data))
            previousLocation = makeLatLng(data)
        } else {
            locationHistory!!.add(makeLatLng(data))
        }
        drawPolyline(makeLatLng(data))
        Log.d("GPS", locationHistory.toString())
    }

}