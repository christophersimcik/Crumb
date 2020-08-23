package com.example.crumb.Helpers

import android.graphics.Color
import kotlin.random.Random

class ColorHelper {

    fun aRandomColor() : Int {
        val alpha = 200
        val red = Random.nextInt(25, 255)
        val green = Random.nextInt(25, 255)
        val blue = Random.nextInt(25, 255)
        return   Color.argb(alpha, red, green, blue)
    }
}