package com.example.runningrhino

import android.location.Location

interface TrackingCallback {
    fun onDataReceived(data: Location)
}