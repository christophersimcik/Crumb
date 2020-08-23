package com.example.crumb.UI

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager

class CustomLayoutManager(context: Context?) : LinearLayoutManager(context) {
    private var scrollEnabled = true

    override fun canScrollVertically(): Boolean {
        return scrollEnabled && super.canScrollVertically()
    }
}