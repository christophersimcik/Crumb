package com.example.crumb

import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable

class itemDrawable(w : Int, h : Int, border : Int, tail : Int, radius : Float, activeColor : Int, inactiveColor : Int) {

    val width = w
    val height = h - tail
    val tail = tail
    var radii = Array(8){ i -> radius} as FloatArray
    val border = border
    val activeColor = ColorStateList.valueOf(activeColor)
    val inactiveColor = ColorStateList.valueOf(inactiveColor)

    fun createBody(active : Boolean) : GradientDrawable{
        val gradientDrawable = GradientDrawable()
        gradientDrawable.shape = GradientDrawable.RECTANGLE
        gradientDrawable.cornerRadii = radii
        gradientDrawable.setStroke(border,border)
        gradientDrawable.setSize(width,height)
        if(active){
            gradientDrawable.color = activeColor
        }else{
            gradientDrawable.color = inactiveColor
        }

        return gradientDrawable
    }

    fun createTail(active : Boolean) : GradientDrawable{
        val gradientDrawable = GradientDrawable()
        gradientDrawable.shape = GradientDrawable.LINE
        gradientDrawable.setStroke(border,border)
        gradientDrawable.setSize(1,tail)
        if(active){
            gradientDrawable.color = activeColor
        }else{
            gradientDrawable.color = inactiveColor
        }
        return gradientDrawable
    }
}