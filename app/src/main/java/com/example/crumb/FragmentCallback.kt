package com.example.crumb

import androidx.fragment.app.Fragment

interface FragmentCallback {
    fun fragmentAttached(fragment : Fragment)
}