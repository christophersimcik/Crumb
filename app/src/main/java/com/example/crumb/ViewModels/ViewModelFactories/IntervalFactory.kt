package com.example.crumb.ViewModels.ViewModelFactories

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.crumb.ViewModels.IntervalViewModel

class IntervalFactory(val application : Application, var  id : Long) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return IntervalViewModel(application, id) as T
    }
}