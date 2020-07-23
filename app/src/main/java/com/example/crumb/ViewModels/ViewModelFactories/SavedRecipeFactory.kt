package com.example.crumb.ViewModels.ViewModelFactories

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.crumb.ViewModels.SavedRecipeViewModel

class SavedRecipeFactory(application : Application, id : Long) : ViewModelProvider.NewInstanceFactory() {
    val application = application
    var id = id

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return SavedRecipeViewModel(application, id) as T
    }
}