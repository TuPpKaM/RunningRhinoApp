package com.example.runningrhino.tracking

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.osmdroid.views.overlay.Polyline
import java.time.LocalDateTime

class TrackingViewModel : ViewModel() {

    private val _lineList = MutableLiveData(ArrayList<Polyline>())
    val lineList: LiveData<ArrayList<Polyline>> = _lineList

    private val _fix = MutableLiveData(false)
    val fix: LiveData<Boolean> = _fix

    private val _tracking = MutableLiveData(false)
    val tracking: LiveData<Boolean> = _tracking

    private val _startTime = MutableLiveData(LocalDateTime.now())
    val startTime: LiveData<LocalDateTime> = _startTime

    fun addLine(line: Polyline) {
        val currentList = _lineList.value ?: ArrayList()
        currentList.add(line)
        _lineList.value = currentList
    }

    fun startTracking() {
        _tracking.value = true
        _startTime.value = LocalDateTime.now()
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
}