package com.example.runningrhino.tracking

import android.location.Location

interface TrackingCallback {
    fun onDataReceived(data: Location)
}