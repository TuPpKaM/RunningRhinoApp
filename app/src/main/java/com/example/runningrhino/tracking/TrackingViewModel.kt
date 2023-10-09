package com.example.runningrhino.tracking

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import org.osmdroid.views.overlay.Polyline
import java.time.LocalDateTime

class TrackingViewModel(private val state: SavedStateHandle) : ViewModel() {

    private val _lineList = MutableLiveData(ArrayList<Polyline>())
    val lineList: LiveData<ArrayList<Polyline>> = _lineList

    private val _fix = MutableLiveData(false)
    val fix: LiveData<Boolean> = _fix

    private val _tracking = MutableLiveData(false)
    val tracking: LiveData<Boolean> = _tracking

    private val _startTime = MutableLiveData(LocalDateTime.now().toString())
    val startTime: LiveData<String> = _startTime

    fun addLine(line: Polyline) {
        val currentList = _lineList.value ?: ArrayList()
        currentList.add(line)
        _lineList.value = currentList
    }

    fun startTracking() {
        _tracking.value = true
        _startTime.value = LocalDateTime.now().toString()
    }

    fun setFix(fixState: Boolean) {
        _fix.value = fixState
    }

    fun getLineListLen(): Int {
        return _lineList.value?.size ?: 0
    }

    fun getLineList(): ArrayList<Polyline> {
        return _lineList.value ?: ArrayList()
    }

    fun saveState(outState: Bundle) {
        //TODO:: saved state for lineList
        outState.putBoolean("fixKey", _fix.value ?: false)
        outState.putBoolean("trackingKey", _tracking.value ?: false)
        outState.putString("startTimeKey", _startTime.value)
    }

    fun restoreState(savedInstanceState: Bundle?) {
        //TODO:: saved state for lineList
        _fix.value = savedInstanceState?.getBoolean("fixKey")
        _tracking.value = savedInstanceState?.getBoolean("trackingKey")
        _startTime.value = savedInstanceState?.getString("startTimeKey")
    }
}