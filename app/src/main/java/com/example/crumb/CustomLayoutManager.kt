package com.example.crumb

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager

class CustomLayoutManager(context: Context?) : LinearLayoutManager(context) {
    private var scrollEnabled = true
    fun setScrollEnabled(bool : Boolean){
        scrollEnabled = bool
    }
    override fun canScrollVertically(): Boolean {
        return scrollEnabled && super.canScrollVertically()
    }
}