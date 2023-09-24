package com.example.runningrhino.running

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class EndRunViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "aaaa"
    }
    val text: LiveData<String> = _text
}