package com.example.runningrhino

import android.graphics.Color

abstract class Constants {
    companion object {
        const val DEBUG: Boolean = true
        const val LOCATION_UPDATE_FREQ: Long = 5000L
        const val CAMERA_UPDATE_FREQ: Int = 5
        const val START_ZOOM: Int = 18
        const val STROKE_COLOR: Int = Color.CYAN
        const val STROKE_WIDTH: Float = 5F
        const val FOREGROUND_SERVICE_CHANNEL: String = "10" //TODO: permanent solution
        const val FOREGROUND_SERVICE_NOTIFICATION_ID: Int = 11
    }
}