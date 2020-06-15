package com.example.tightboules

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class PlayFactory(application : Application, id : Long) : ViewModelProvider.NewInstanceFactory() {
    val application = application
    var id = id

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return PlayViewModel(application, id) as T
    }
}