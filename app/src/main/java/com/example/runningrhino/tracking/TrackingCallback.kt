package com.example.runningrhino.tracking

import android.location.Location

interface TrackingCallback {

    fun onFix(data: Boolean)
    fun onDataReceived(data: Location)
}