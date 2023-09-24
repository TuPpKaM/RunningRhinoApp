package com.example.runningrhino

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.runningrhino.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, TrackingCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var mService: TrackingService
    private var mBound: Boolean = false

    private var locationHistory: ArrayList<Location> = ArrayList<Location>()
    private var previousLocation: Location? = null
    private var polylines: ArrayList<Polyline> = ArrayList<Polyline>()

    private var distance: Float = 0F

    private var debugTestInt: Int = 0

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

        setSupportActionBar(findViewById(R.id.my_toolbar))
        supportActionBar?.setDisplayShowTitleEnabled(false)

        //val navController = findNavController(R.id.nav_host_fragment)
        //val appBarConfiguration = AppBarConfiguration(navController.graph)

        //val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        //val navController = navHostFragment.navController
        //setupActionBarWithNavController(navController)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar, menu)
        return true
    }

    override fun onMapReady(googleMap: GoogleMap) {
        Log.d("GPS", "onMapReady")
        mMap = googleMap

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        Intent(this, TrackingService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //val navController = findNavController(R.id.nav_host_fragment)
        when (item.itemId) {
            R.id.action_settings -> {
                //navController.navigate(R.id.action_mapsActivity_to_startRunFragment)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
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

    private fun debugGps(location: Location): Location {
        val lat = location.latitude + (debugTestInt * 0.0001)
        val lng = location.longitude - (debugTestInt * 0.0001)

        location.latitude = lat
        location.longitude = lng

        debugTestInt += 1

        return location
    }

    /**
     * Draws a continuous polyline on the Map with each update
     * @param currentLocation Draws a polyline to this location from previous location
     */
    private fun drawPolyline(currentLocation: Location) {
        val lineOptions = PolylineOptions()
            .add(LatLng(previousLocation!!.latitude, previousLocation!!.longitude))
            .add(LatLng(currentLocation.latitude, currentLocation.longitude))
            .color(Color.RED)
            .width(5f)

        val polyline: Polyline = mMap.addPolyline(lineOptions)
        polylines.add(polyline)

        registerDistance(currentLocation, previousLocation!!)

        previousLocation = currentLocation
    }

    /**
     * Adds a marker on the first position
     * @param start Location
     */
    private fun drawStartMarker(start: Location) {
        previousLocation = start
        val markerOptions = MarkerOptions()
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
        mMap.addMarker(
            markerOptions.position(LatLng(start.latitude, start.longitude)).title("Start")
        )
        mMap.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(start.latitude, start.longitude),
                16F
            )
        )
    }

    /**
     * Callback from TrackingService. Saves the new cordinates in the history, calls
     * for the distance to be drawn on the map and added to total distance.
     * @param location Location
     */
    override fun onDataReceived(data: Location) {
        var location = data

        if (Constants.DEBUG) {
            location = Location(debugGps(data))
        }

        Log.d("GPS", "-----${location}")

        if (locationHistory.isEmpty()) {
            drawStartMarker(location)
        }

        locationHistory.add(location)

        drawPolyline(location)
    }

    private fun registerDistance(p1: Location, p2: Location) {
        Log.d("GPS", "Before:${distance}m")
        distance += p1.distanceTo(p2)
        Log.d("GPS", "After:${distance}m")

    }

}