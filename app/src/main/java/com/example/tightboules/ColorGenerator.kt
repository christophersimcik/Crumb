package com.example.tightboules

import android.graphics.Color
import kotlin.random.Random

class ColorGenerator {

    fun makeColors(size : Int) : ArrayList<Int>{
        val colors = ArrayList<Int>()
        var cols : IntArray

        while(colors.size < size){
            cols = randomColor()
            if(colors.size > 2){
                compareValues(colors.get(colors.size-2),cols[0])
                }
            colors.add(cols[0])
            colors.add(cols[1])
            }

        return colors
    }

    fun randomColor() : IntArray {
        val colorArray = IntArray(2)
        val alpha = 255
        val red = Random.nextInt(0, 255)
        val green = Random.nextInt(0, 255)
        val blue = Random.nextInt(0, 255)
        colorArray[0] = Color.argb(alpha, red, green, blue)
        colorArray[1] = getCompliment(colorArray[0])

        return colorArray
    }

    fun getCompliment(col: Int): Int {
        val alpha = 255
        val red = 255 - Color.red(col)
        val green = 255 - Color.green(col)
        val blue = 255 - Color.blue(col)
        return Color.argb(alpha, red, green, blue)
    }

    fun compareValues(oldCol : Int, newCol : Int) : IntArray{
        val oldHSV = FloatArray(3)
        val newHSV = FloatArray(3)
        val newCols = IntArray(2)
        Color.colorToHSV(oldCol, oldHSV)
        Color.colorToHSV(newCol, newHSV)
        if(Math.abs(newHSV[2]-oldHSV[2])>100){
            oldHSV[2] = oldHSV[2] - oldHSV[2]/2
            newHSV[2] = newHSV[2] + newHSV[2]/2
        }
        newCols[0] = Color.HSVToColor(oldHSV)
        newCols[1] = Color.HSVToColor(newHSV)

        return newCols
    }

}