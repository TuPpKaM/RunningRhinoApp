package com.example.runningrhino

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.runningrhino.databinding.ActivityMapsBinding
import com.example.runningrhino.tracking.TrackingCallback
import com.example.runningrhino.tracking.TrackingService
import com.example.runningrhino.tracking.TrackingViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Polyline


class MainActivity : AppCompatActivity(), TrackingCallback {

    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var mService: TrackingService
    private var mBound: Boolean = false

    private val sharedViewModel: TrackingViewModel by viewModels()

    private var locationHistory: ArrayList<Location> = ArrayList<Location>()
    private var previousLocation: Location? = null
    private var lineList: ArrayList<Polyline> = ArrayList<Polyline>()

    private var distance: Float = 0F

    private var debugTestInt: Int = 0

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            Log.d("GPS", "onServiceConnected")
            val binder = service as TrackingService.LocalBinder
            mService = binder.getService()
            mBound = true
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

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        setupActionBarWithNavController(navController)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        Intent(this, TrackingService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }

        sharedViewModel.tracking.observe(this) {
            if (it && checkLocationPermissions() && mBound) {
                Log.d("GPS", "start tracking observed, time = ${sharedViewModel.startTime.value}")
                startLocationTracking()
            }
        }

        checkNotificationPermission()
        createNotificationChannel(Constants.FOREGROUND_SERVICE_CHANNEL)

        navController.navigate(R.id.mapsFragment)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        when (item.itemId) {
            R.id.action_settings -> {
                navController.navigate(R.id.action_mapsFragment_to_startRunFragment)
                return true
            }
            R.id.action_about -> {
                navController.navigate(R.id.action_startRunFragment_to_mapsFragment)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun checkNotificationPermission(): Boolean {
        Log.d("GPS", "checkNotificationPermissions")
        Log.d(
            "GPS",
            "Notification${
                ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                )
            }"
        )
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.FOREGROUND_SERVICE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.FOREGROUND_SERVICE
                ),
                101
            )
            return false
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions( //TODO:
                this,
                arrayOf(
                    android.Manifest.permission.POST_NOTIFICATIONS
                ),
                101
            )
            return false
        }
        return true
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.action == "BUTTON_CLICK_ACTION_STOP") {
            Log.d("GPS", "BUTTON_CLICK_ACTION_STOP")
        }
    }

    private fun createNotificationChannel(cid: String) {
        //TODO: REMAKE

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is not in the Support Library.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val notificationchannel = NotificationChannel(cid, name, importance).apply {
                description = descriptionText
            }

            // Register the channel with the system.
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationchannel)
        }
    }

    private fun checkLocationPermissions(): Boolean {
        //TODO: REMAKE

        Log.d("GPS", "checkLocationPermissions")
        Log.d(
            "GPS",
            "FINE${
                ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                )
            }"
        )
        Log.d(
            "GPS",
            "COURSE${
                ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                )
            }"
        )
        Log.d(
            "GPS",
            "STATE${
                ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_NETWORK_STATE
                )
            }"
        )
        Log.d(
            "GPS",
            "INTERNET${
                ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.INTERNET
                )
            }"
        )
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(
                "GPS",
                "MISSING location permission ${
                    ActivityCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                }"
            )
            ActivityCompat.requestPermissions(
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
        mService.setCallback(this@MainActivity)
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
     */
    private fun drawPolyline(currentLocation: Location) {
        Log.d("GPS", "drawPolyLine ${currentLocation}")
        val geoPoints = arrayListOf(
            GeoPoint(currentLocation.latitude, currentLocation.longitude), GeoPoint(
                previousLocation!!.latitude, previousLocation!!.longitude
            )
        )
        val line = Polyline()
        line.setPoints(geoPoints)
        sharedViewModel.addLine(line)
        Log.d("GPS", "drawPolyLine ${line}")

        registerDistance(currentLocation, previousLocation!!)

        previousLocation = currentLocation
    }

    /**
     * Adds a marker on the first position
     */
    private fun drawStartMarker(start: Location) {
        previousLocation = start
        //add marker
    }

    override fun onFix(data: Boolean) {
        Log.d("GPS", "service replied, fix = ${data}")
        sharedViewModel.setFix(data)
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