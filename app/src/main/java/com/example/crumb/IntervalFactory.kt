package com.example.crumb

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class IntervalFactory(application : Application, id : Long) : ViewModelProvider.NewInstanceFactory() {
    val application = application
    var id = id

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return IntervalViewModel(application, id) as T
    }
}