package com.example.crumb

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class InterFragmentObservable : ViewModel() {
    val parentID =  MutableLiveData<Long>()

    fun setParentID(parentID : Long){
        this.parentID.value = parentID
    }
}